package org.madnl.iota.module.echo

import org.madnl.iota.api.{BodyReader, PreambleWriter, Request, ResourceDriver}
import akka.util.{ByteStringBuilder, ByteString}
import org.madnl.iota.http.{Header, HeaderMap, StatusCodes}
import com.google.common.net.HttpHeaders

/**
 * Simple driver which echoes the request as HTML
 */
object EchoDriver extends ResourceDriver {
  /**
   * Called when a new request has arrived for this driver
   * @param request The request preamble
   * @param preambleWriter An object with which the response can be written
   * @return A body reader that will be used to read the body
   */
  def onRequest(request: Request, preambleWriter: PreambleWriter) = {
    val buffer = new ByteStringBuilder

    new BodyReader {
      def onBodyFinished() {
        preambleWriter.simpleWrite(
          status = StatusCodes.Ok,
          reason = "Ok",
          headers = HeaderMap(HttpHeaders.CONTENT_TYPE -> "text/html"),
          contentUtf8 = buildResponse(request, buffer.result())
        )
      }

      def onBodyPart(data: ByteString) {
        buffer.append(data)
      }
    }
  }

  private def buildResponse(request: Request, body: ByteString) = {
    val response =
      <html>
        <body>
          <h1>Echo service</h1>
          <p>A request with method <b>{request.method}</b> was made to the following URI:</p>
          <pre>
            {request.uri}
          </pre>
          <p>The headers of the request were:</p>
          <table border="1">
          {for (Header(name, value) <- request.headers) yield
            <tr>
              <td>{name}</td><td>{value}</td>
            </tr>
          }
          </table>
          <p>The content has mime type {request.headers.get(HttpHeaders.CONTENT_TYPE).getOrElse("Unknown") }. The UTF8
          content is reproduced below:</p>
          <pre>
            {body.utf8String}
          </pre>
        </body>
      </html>
    response.toString() + "\n"
  }
}
