package com.adobe.iota.server

import com.adobe.iota.api.Request

/**
 * Logger object which logs incoming requests
 */
trait RequestLogger {

  def log(request: Request)

}
