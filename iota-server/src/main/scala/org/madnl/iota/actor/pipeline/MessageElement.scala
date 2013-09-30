package org.madnl.iota.actor.pipeline

import java.net.URI
import org.madnl.iota.http.{Version, Header, HeaderMap}
import akka.util.ByteString
import org.madnl.iota.api.{Response, Request}

/**
 * Messages exchanged in the HTTP pipelines
 */
sealed trait MessageElement

/**
 * Higher level messages, sent towards resource drivers
 */
sealed trait RequestPart extends MessageElement

/**
 * The initial part of a request
 * @param request the request
 * @param protocolVersion the client's protocol version
 */
case class RequestPreamble(request: Request, protocolVersion: Version) extends RequestPart

object RequestPreamble {
  def apply(method: String, uri: URI, headers: HeaderMap, protocolVersion: Version): RequestPreamble =
    RequestPreamble(Request(method, uri, headers), protocolVersion)
}

/**
 * Indicates the end of a request
 */
case object EndOfRequest extends RequestPart with RequestSegment

/**
 * Higher level messages, sent towards the pipeline
 */
sealed trait ResponsePart extends MessageElement

/**
 * The initial part of the response
 * @param response The response
 */
case class ResponsePreamble(response: Response) extends ResponsePart

object ResponsePreamble {
  def apply(status: Int, reason: String, headers: HeaderMap): ResponsePreamble =
    ResponsePreamble(Response(status, reason, headers))
}

/**
 * Indicates the end of the response
 */
case object EndOfResponse extends ResponsePart

/**
 * Lower-level message containing parts of a request
 */
sealed trait RequestSegment extends MessageElement

/**
 * The request line of a request
 * @param method the HTTP method
 * @param uri the request uri
 * @param protocolVersion the protocol version
 */
case class RequestLine(method: String, uri: URI, protocolVersion: Version) extends RequestSegment

/**
 * A particular header
 * @param header the header object
 */
case class HeaderSegment(header: Header) extends RequestSegment

/**
 * Indicates that all the headers have been read
 */
case object EndOfHeader extends RequestSegment

/**
 * Lower level messages indicating parts of the response
 */
sealed trait ResponseSegment extends MessageElement

/**
 * The byte string data of the response preamble
 * @param data the data containing the response line & headers
 */
case class FullHeader(data: ByteString) extends ResponseSegment

/**
 * Part of a request or response body
 * @param data the binary data
 */
case class BodyPart(data: ByteString) extends RequestSegment with ResponseSegment with RequestPart with ResponsePart

