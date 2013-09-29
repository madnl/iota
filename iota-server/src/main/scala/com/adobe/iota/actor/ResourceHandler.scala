package com.adobe.iota.actor

import akka.actor._
import akka.io.TcpPipelineHandler.{WithinActorContext, Init}
import com.adobe.iota.actor.pipeline._
import com.adobe.iota.api._
import akka.util.ByteString
import com.adobe.iota.actor.pipeline.RequestPreamble
import com.adobe.iota.actor.pipeline.BodyPart
import com.adobe.iota.api.Response
import com.adobe.iota.server.Controller
import java.net.InetSocketAddress
import com.google.common.net.{HttpHeaders, HostAndPort}

/**
 * Handles the request-response lifecycle of resource drivers creating during the span of a connection
 */
private[actor] class ResourceHandler(controller: Controller, init: Init[WithinActorContext, ResponsePart, RequestPart], interface: InetSocketAddress)
  extends Actor with Stash with ActorLogging {
  import ResourceHandler._
  import context._

  def receive = awaitNewRequest

  //The initial state of the actor
  private def awaitNewRequest: Receive = {
    case init.Event(RequestPreamble(request, _)) => {
      controller.logger.log(request)
      val connection = sender
      val resolver = controller.resourceResolver
      val driver = resolver.resolve(getHostAndPort(request), request.uri)
      val bodyReader = driver.onRequest(request, new Writer(connection))
      become(receiveBody(bodyReader, connection))
    }
  }

  //state in which we receive the body parts of the request
  private def receiveBody(bodyReader: BodyReader, connection: ActorRef): Receive = {
    case init.Event(BodyPart(data)) => {
      bodyReader.onBodyPart(data)
    }
    case init.Event(EndOfRequest) => {
      bodyReader.onBodyFinished()
      become(awaitResponseTermination(connection))
    }
    case ResponseFinished => {
      //response can be received before body was read
      signalEndOfResponse(connection)
      become(ignoreRestOfBody)
    }
  }

  //state occurring after the body has been read. We are waiting for the driver to finish the response. In the
  //meantime we stash all incoming request events so that we can process them after we are done with the
  //current request
  private def awaitResponseTermination(connection: ActorRef): Receive = {
    case init.Event(_) => {
      stash() //stash any events regarding a new request (this is possible since the server supports pipelineing)
    }
    case ResponseFinished => {
      signalEndOfResponse(connection)
      restartCycle()
    }
  }

  //special state triggered by an early response from the driver
  private def ignoreRestOfBody: Receive = {
    case init.Event(BodyPart(_)) => ()
    case init.Event(EndOfRequest) => restartCycle()
  }

  private def signalEndOfResponse(connection: ActorRef) {
    connection ! init.command(EndOfResponse)
  }

  private def restartCycle() {
    become(awaitNewRequest)
    unstashAll()
  }

  private def getHostAndPort(request: Request) = {
    request.headers.get(HttpHeaders.HOST) match {
      case None => HostAndPort.fromParts(interface.getHostName, interface.getPort)
      case Some(str) => HostAndPort.fromString(str)
    }
  }


  class Writer(connection: ActorRef) extends PreambleWriter with BodyWriter {

    private var bodyFinished = false

    def writePreamble(response: Response) = {
      connection ! init.command(ResponsePreamble(response))
      this
    }

    def writeBodyPart(data: ByteString) = {
      if (bodyFinished) {
        throw new IllegalStateException("Cannot write body part after calling the finishBody method")
      }
      connection ! init.command(BodyPart(data))
      this
    }

    def finishBody() {
      if (bodyFinished) {
        throw new IllegalStateException("The finishedBody method was already called")
      }
      bodyFinished = true
      self ! ResponseFinished
    }
  }

}

private[actor] object ResourceHandler {

  /**
   * Provides a props for the resource handler actor.
   * @param controller The server controller
   * @param init An init object which links in a type safe manner the resulting actor with the connection actor
   * @return a props for a resource handler
   */
  def apply(controller: Controller, interface: InetSocketAddress, init: Init[WithinActorContext, ResponsePart, RequestPart]): Props =
    Props(new ResourceHandler(controller, init, interface)).withMailbox("akka.custom-mailboxes.dequeue-based")

  //Event that indicates to the resource handler that the driver is finished
  private case object ResponseFinished

}
