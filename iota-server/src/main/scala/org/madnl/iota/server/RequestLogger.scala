package org.madnl.iota.server

import org.madnl.iota.api.Request

/**
 * Logger object which logs incoming requests
 */
trait RequestLogger {

  def log(request: Request)

}
