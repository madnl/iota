package org.madnl.iota.actor.pipeline.segment

import org.madnl.iota.actor.pipeline._
import akka.util.ByteString
import org.madnl.iota.Protocol
import org.madnl.iota.http.{Methods, Header}
import akka.io.HasLogging
import com.google.common.net.HttpHeaders

/**
 * The event pipe of the segment stage
 */
trait SegmentEventPipe extends StateMachine[ResponseSegment, ByteString, RequestSegment, ByteString] {

  becomeEvt(EventPipe {
    case data: ByteString => eventMachine.feed(data)
  })

  val eventMachine = new EventMachine[Result] with BodyTransferSemantics {

    def initially = requestLine

    def requestLine: State = () => {
      consumeUntil(LineSeparator) match {
        case None => ()
        case Some(line) => {
          val rl = DataManipulation.readRequestLine(line)
          detectBodyTransferFromMethod(rl.method)
          emit(rl)
          become(startHeaders)
        }
      }
    }

    private def startHeaders = () => {
      consumeUntil(LineSeparator) match {
        case None => ()
        case Some(line) if line.isEmpty => {
          emit(EndOfHeader)
          switchToBodyTransfer()
        }
        case Some(line) => become(continueHeaders(line))
      }
    }

    private def continueHeaders(currentHeaderAccum: ByteString): State = () => {
      consumeUntil(LineSeparator) match {
        case None => ()
        case Some(line) if line.isEmpty => {
          emitHeader(currentHeaderAccum)
          emit(EndOfHeader)
          switchToBodyTransfer()
        }
        case Some(line) if isHeaderContinuation(line) => {
          //headers can span multiple lines
          become(continueHeaders(currentHeaderAccum ++ line))
        }
        case Some(line) => {
          emitHeader(currentHeaderAccum)
          become(continueHeaders(line))
        }
      }
    }

    private def readFixedBody(remaining: Int)() {
      assert(remaining > 0)
      val chunk = consumeAtMost(remaining)
      if (!chunk.isEmpty) {
        emit(BodyPart(chunk))
        val newRemaining = remaining - chunk.size
        if (newRemaining > 0) {
          become(readFixedBody(newRemaining))
        } else {
          resetRequestCycle()
        }
      }
    }

    private def switchToBodyTransfer() {
      bodyTransfer match {
        case NoBody | IdentityTransfer(0) => resetRequestCycle()
        case IdentityTransfer(contentLength) => become(readFixedBody(contentLength))
        case _ => throw new UnsupportedProtocolFeature(s"The transfer method $bodyTransfer is not supported")
      }
    }

    private def resetRequestCycle() {
      emit(EndOfRequest)
      resetBodyTransfer()
      become(requestLine)
    }

    private def emitHeader(bs: ByteString) {
      val header = DataManipulation.readHeader(bs)
      detectSpecialHeader(header)
      emit(HeaderSegment(header))
    }

    private def isHeaderContinuation(bs: ByteString) = {
      !bs.isEmpty && {
        val h = bs.head
        h == Protocol.SP || h == Protocol.HT
      }
    }


    private def detectBodyTransferFromMethod(method: String) {
      if (Methods.hasNoBody(method)) {
        establishBodyTransfer(NoBody)
      }
    }

    private def detectSpecialHeader(header: Header) {
      header.name match {
        case HttpHeaders.CONTENT_LENGTH => {
          val contentLength = header.value.toInt
          establishBodyTransfer(IdentityTransfer(contentLength))
        }
        case _ => ()
      }
    }

    private def emit(segment: RequestSegment) {
      emit(Left(segment))
    }

    private val LineSeparator = Encoding.preambleEncoded(Protocol.LineSeparator)
  }
}
