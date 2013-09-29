package com.adobe.iota.actor.pipeline.segment

import akka.util.{ByteStringBuilder, ByteString}
import com.adobe.iota.http.{Version, Header}
import java.net.URI
import scala.annotation.tailrec
import com.adobe.iota.Protocol
import com.adobe.iota.actor.pipeline.{ProtocolClientException, RequestLine, Encoding}

/**
 * Useful methods to encode/decode byte strings to relevant data structures
 */
private[pipeline] object DataManipulation {

  final val LineSeparator = Encoding.preambleEncoded(Protocol.LineSeparator)
  private val HeaderDivider = Encoding.preambleEncoded(Protocol.CommonHeaderDivider)

  def readRequestLine(bs: ByteString): RequestLine = {
    splitTokens(bs) match {
      case Seq(method, uriStr, httpVer) => {
        val uri = URI.create(Encoding.preambleDecode(uriStr))
        val version = Version(Encoding.preambleDecode(httpVer).stripPrefix("HTTP/")) //todo
        RequestLine(Encoding.preambleDecode(method), uri, version)
      }
      case _ => formatError("Invalid format for request line")
    }
  }

  def readHeader(bs: ByteString): Header = {
    bs.indexOf(Protocol.HeaderDivider) match {
      case -1 => invalidHeader(bs)
      case index => {
        val nameBs = {
          val (result, lws) = bs.take(index).span(Protocol.isTokenChar)
          if (lws.forall(Protocol.isLWSChar))
            result
          else
            invalidHeader(bs)
        }
        val valueBs = lwsTrim(bs.drop(index+1))
        if (nameBs.isEmpty || valueBs.isEmpty)
          invalidHeader(bs)
        else
          Header(Encoding.preambleDecode(nameBs), Encoding.preambleDecode(valueBs))
      }
    }
  }

  private def lwsTrim(bs: ByteString) = {
    val bs2 = lwsLeftTrim(bs)
    math.max(bs2.lastIndexOf(Protocol.HT), bs2.lastIndexOf(Protocol.SP)) match {
      case -1 => bs2
      case index => bs2.take(index)
    }
  }

  private def lwsLeftTrim(bs: ByteString) = bs.dropWhile(Protocol.isLWSChar)

  def writeHeader(header: Header, builder: ByteStringBuilder): Unit = {
    writeHeader(header.name, header.value, builder)
  }

  def writeHeader(name: String, value: String, builder: ByteStringBuilder): Unit = {
    builder
      .append(Encoding.preambleEncoded(name))
      .append(HeaderDivider)
      .append(Encoding.preambleEncoded(value))
      .append(LineSeparator)
  }

  def writeResponseLine(status: Int, reason: String, protocol: Version, builder: ByteStringBuilder): Unit = {
    val str = s"HTTP/${protocol.major}.${protocol.minor} $status $reason"
    builder.append(Encoding.preambleEncoded(str)).append(LineSeparator)
  }

  private def splitTokens(bs: ByteString): Seq[ByteString] = {

    @tailrec
    def split(bs: ByteString, accum: List[ByteString]): List[ByteString] = {
      bs.indexOf(Protocol.SP) match {
        case -1 => (bs :: accum).reverse
        case index => {
          val token = bs.take(index)
          split(bs.drop(index+1), token :: accum)
        }
      }
    }

    split(bs, List())
  }

  private def invalidHeader(bs: ByteString) = formatError(s"Bad Header: [${bs.utf8String}]")

  private def formatError(message: String) = throw new ProtocolClientException(message)
}
