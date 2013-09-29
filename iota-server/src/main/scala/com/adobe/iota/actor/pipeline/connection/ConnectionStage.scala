package com.adobe.iota.actor.pipeline.connection

import akka.io.{HasActorContext, HasLogging, PipelineStage}
import com.adobe.iota.actor.pipeline._
import com.adobe.iota.api.Request
import com.google.common.net.HttpHeaders
import com.adobe.iota.http.{Version, Header}

/**
 * This pipeline stage handles aspects related to the connection keep-alive mode. It does not do any data manipulation
 */
class ConnectionStage(control: ConnectionControl)
  extends PipelineStage[HasLogging with HasActorContext, ResponsePart, ResponsePart, RequestPart, RequestPart] {
  import ConnectionStage._

  override def apply(ctx: HasLogging with HasActorContext) = new StateMachine[ResponsePart, ResponsePart, RequestPart, RequestPart] {

    private val keepAliveHeader = Header("Keep-Alive", s"timeout=${control.keepAliveTimeoutMs}")

    private var pendingRequests = 0 //indicates how many requests haven't received yet an answer on this connection

    //flag indicating if new requests should be ignored and the connection should be closed after sending
    //all the responses
    private var closingTime = false

    becomeEvt(awaitPreamble)
    becomeCmd(processResponsePreamble)

    // EVENT PIPE

    //initial state - awaiting new request
    private def awaitPreamble: EventPipe = EventPipe {
      case rp@RequestPreamble(request, version) => {
        pendingRequests = pendingRequests + 1
        control.notifyOfNewRequest()
        val keepAlive = detectKeepAlive(request, version)
        closingTime = !keepAlive || closingTime
        becomeEvt(processBody)
        ctx.singleEvent(rp)
      }
    }

    //processing the current request body
    private def processBody = EventPipe {
      case EndOfRequest => {
        if (closingTime) {
          becomeEvt(ignoreEverything)
        } else {
          becomeEvt(awaitPreamble)
        }
        ctx.singleEvent(EndOfRequest)
      }
      case evt => ctx.singleEvent(evt)
    }

    //state of the event pipe after closing time
    private def ignoreEverything = EventPipe {
      case _ => ctx.nothing
    }

    // COMMAND PIPE

    //initial state for the command pipe - awaiting new response
    private def processResponsePreamble: CommandPipe = CommandPipe {
      case ResponsePreamble(response) => {
        pendingRequests = pendingRequests - 1
        becomeCmd(processResponseBody)
        val headers = response.headers
        val newHeaders =
          if (connectionCanBeClosed)
            headers.withHeader(HttpHeaders.CONNECTION, Close)
          else
            headers.withHeader(keepAliveHeader.name, keepAliveHeader.value)
        val newResponse = response.copy(headers = newHeaders)
        ctx.singleCommand(ResponsePreamble(newResponse))
      }
    }

    //awaiting for the body of the response
    private def processResponseBody = CommandPipe {
      case EndOfResponse => {
        if (connectionCanBeClosed) {
          control.close()
          becomeCmd(guard)
        } else {
          becomeCmd(processResponsePreamble)
        }
        ctx.singleCommand(EndOfResponse)
      }
      case bp: BodyPart => ctx.singleCommand(bp)
    }

    //making sure no response is sent after closing time
    private def guard = CommandPipe {
      case _ => throw new IllegalStateException()
    }

    private def detectKeepAlive(request: Request, version: Version) = {
      val value = request.headers.get(HttpHeaders.CONNECTION).getOrElse("")
      if (version == LegacyVersion)
        value equalsIgnoreCase KeepAlive
      else
        !(value equalsIgnoreCase Close)
    }

    override def managementPort = {
      case ConnectionStage.EnterClosingMode => {
        //message sent as a result of the keep-alive timeout
        closingTime = true
        ctx.nothing
      }
    }

    private def connectionCanBeClosed = closingTime && pendingRequests == 0
  }
}

object ConnectionStage {
  private val LegacyVersion = Version(1, 0)
  private val Close = "close"
  private val KeepAlive = "keep-alive"

  //Management command indicating that the pipeline should enter closing time
  case object EnterClosingMode
}