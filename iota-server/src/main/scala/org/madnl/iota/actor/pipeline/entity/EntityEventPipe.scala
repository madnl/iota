package org.madnl.iota.actor.pipeline.entity

import org.madnl.iota.actor.pipeline._
import org.madnl.iota.http.{HeaderMap, Header}
import scala.collection.mutable.ListBuffer
import org.madnl.iota.api.Request
import akka.io.{HasActorContext, HasLogging}

/**
 * The event pipe for the entity stage
 */
trait EntityEventPipe extends StateMachine[ResponsePart, ResponseSegment, RequestPart, RequestSegment]
  with HasContext[HasActorContext] {

  becomeEvt(readRequestLine)

  private def readRequestLine: EventPipe = EventPipe {
    case rl: RequestLine => {
      becomeEvt(readHeaders(rl))
      context.nothing
    }
  }

  private def readHeaders(requestLine: RequestLine) = new EventPipe {

    val headers = ListBuffer[Header]()

    def handle = {
      case HeaderSegment(header) => {
        headers += header
        context.nothing
      }
      case EndOfHeader => {
        val request = Request(requestLine.method, requestLine.uri, HeaderMap(headers))
        becomeEvt(readBody)
        context.singleEvent(RequestPreamble(request, requestLine.protocolVersion))
      }
    }
  }

  private def readBody = EventPipe {
    case bp: BodyPart => context.singleEvent(bp)
    case EndOfRequest => {
      becomeEvt(readRequestLine)
      context.singleEvent(EndOfRequest)
    }
  }
}
