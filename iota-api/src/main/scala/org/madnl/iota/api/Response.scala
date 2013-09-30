package org.madnl.iota.api

import org.madnl.iota.http.HeaderMap

/**
 * An object encapsulating a HTTP response preamble
 * @param statusCode The status code of the response
 * @param reasonPhrase The reason phrase associated with the status code
 * @param headers The response headers
 */
case class Response(statusCode: Int, reasonPhrase: String, headers: HeaderMap)
