package org.madnl.iota.api

import java.net.URI
import org.madnl.iota.http.HeaderMap

/**
 * An object encapsulating an HTTP request preamble
 * @param method The HTTP method of the request
 * @param uri The URI of the request
 * @param headers The HTTP headers of the request
 */
case class Request(method: String, uri: URI, headers: HeaderMap)
