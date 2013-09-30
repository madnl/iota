package org.madnl.iota.actor.pipeline

import akka.util.{ByteStringBuilder, ByteString}
import org.madnl.iota.Protocol
import org.madnl.iota.api.Request

/**
 *
 */
case class TestRequest(method: String, path: String, headers: Seq[(String, String)], body: ByteString) {

  def toByteString: ByteString = {
    val requestLine = s"$method $path HTTP/1.1${Protocol.LineSeparator}"
    val builder = new ByteStringBuilder
    val headerStr = headers.map({case (k, v) => s"$k: $v${Protocol.LineSeparator}"}).mkString("", "", Protocol.LineSeparator)
    builder
      .append(Encoding.preambleEncoded(requestLine))
      .append(Encoding.preambleEncoded(headerStr))
      .append(body)
      .result()
  }

  override def toString = s"TestRequest($method, path(${path.length} chars) headers(#${headers.length}) body(${body.size} B))"


}

object TestRequest {

  def apply(method: String, path: String, headers: Seq[(String, String)], body: String): TestRequest =
    apply(method, path, headers, ByteString(body))

  def apply(request: Request, body: ByteString): TestRequest =
    apply(request.method, request.uri.getPath, request.headers.asPairs, body)
}