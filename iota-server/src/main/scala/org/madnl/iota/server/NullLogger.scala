package org.madnl.iota.server

import org.madnl.iota.api.Request

/**
 * A logger which displays nothing
 */
object NullLogger extends RequestLogger {
  def log(request: Request) {}
}
