package com.adobe.iota.server

import com.adobe.iota.api.Request

/**
 * A logger which displays nothing
 */
object NullLogger extends RequestLogger {
  def log(request: Request) {}
}
