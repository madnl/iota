package org.madnl.iota.api

import akka.util.ByteString
import org.madnl.iota.http.HeaderMap
import com.google.common.net.HttpHeaders._
import com.google.common.net.MediaType

/**
 * Special resource driver which provides the same response for every request
 */
class ConstantResourceDriver private(status: Int, reason: String, body: ByteString) extends ResourceDriver {

  def onRequest(request: Request, preambleWriter: PreambleWriter) = {
    val headers = HeaderMap(
      CONTENT_LENGTH -> body.size.toString,
      CONTENT_TYPE   -> MediaType.PLAIN_TEXT_UTF_8.toString
    )
    val bodyWriter = preambleWriter.writePreamble(Response(status, reason, headers))
    bodyWriter.writeBodyPart(body)
    bodyWriter.finishBody()
    BodyReader.Oblivious
  }

}

object ConstantResourceDriver {

  /**
   * Creates a new resource driver which always answers with the specified parameters
   * @param status The HTTP status code
   * @param reason The response reason phrase
   * @param content The content to be sent in the response
   * @return A constant driver instance
   */
  def apply(status: Int, reason: String, content: String = ""): ResourceDriver =
    new ConstantResourceDriver(status, reason, ByteString(content))

  val NotFound = apply(404, "No such resource", "The specified resource does not exist\n")

  val ServerError = apply(500, "Server error")

}
