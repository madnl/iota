package com.adobe.iota.actor.pipeline.entity

import com.adobe.iota.actor.pipeline._
import com.adobe.iota.api.Response
import akka.util.ByteStringBuilder
import com.adobe.iota.ServerParameters
import com.adobe.iota.http.{Header}
import com.adobe.iota.actor.pipeline.segment.DataManipulation
import akka.io.HasActorContext
import com.google.common.net.HttpHeaders

/**
 * The command pipe for the entity stage
 */
trait EntityCommandPipe extends StateMachine[ResponsePart, ResponseSegment, RequestPart, RequestSegment]
  with HasContext[HasActorContext] {

  becomeCmd(writeResponseHeader)

  private def writeResponseHeader: CommandPipe = CommandPipe {
    case ResponsePreamble(r@Response(status, reason, headers)) => {
      val builder = new ByteStringBuilder
      DataManipulation.writeResponseLine(status, reason, ServerParameters.ProtocolVersion, builder)
      for (Header(name, value) <- headers) {
        DataManipulation.writeHeader(name, value, builder)
      }
      builder.append(DataManipulation.LineSeparator)
      becomeCmd(writeBody(contentLength(r)))
      context.singleCommand(FullHeader(builder.result()))
    }
  }

  private def writeBody(size: Int) = new CommandPipe {

    private var written = 0

    def handle = {
      case bp: BodyPart => {
        written += bp.data.size
        context.singleCommand(bp)
      }
      case EndOfResponse => {
        assert(written == size) //sanity check
        becomeCmd(writeResponseHeader)
        context.nothing
      }
    }
  }

  private def contentLength(response: Response) =
    response.headers.get(HttpHeaders.CONTENT_LENGTH).map(_.toInt).getOrElse(0)
}
