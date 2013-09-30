package org.madnl.iota.api

import org.madnl.iota.http.{Header, HeaderMap}
import akka.util.ByteString
import com.google.common.net.HttpHeaders._
import com.google.common.net.MediaType

/**
 * Object provided to an application to initiate writing the HTTP response.
 */
trait PreambleWriter {

  /**
   * Write the response preamble. Obtains as a result an object with which the body can be written afterwards
   * @param response The http response preamble
   * @return A body writer
   */
  def writePreamble(response: Response): BodyWriter

  /**
   * Shortcut method to write a string response all at once
   * @param status The status code of the response
   * @param reason The reason phrase
   * @param headers The HTTP headers
   * @param contentUtf8 Text content which will be encoded using utf-8
   */
  def simpleWrite(status: Int, reason: String, headers: HeaderMap = HeaderMap(), contentUtf8: String = ""): Unit = {
    val data = ByteString(contentUtf8, "utf-8")
    val hs = headers
      .addIfMissing(CONTENT_LENGTH, data.size.toString)
      .addIfMissing(CONTENT_TYPE, MediaType.PLAIN_TEXT_UTF_8.toString)
    val response = Response(status, reason, hs)
    writePreamble(response).writeBodyPart(data).finishBody()
  }

  /**
   * Shortcut method to write a string-based response all at once
   * @param response The response preamble
   * @param contentUtf8 The string content to be encoded using UTF-8
   */
  def simpleResponseWrite(response: Response, contentUtf8: String = ""): Unit = {
    simpleWrite(response.statusCode, response.reasonPhrase, response.headers, contentUtf8)
  }

}
