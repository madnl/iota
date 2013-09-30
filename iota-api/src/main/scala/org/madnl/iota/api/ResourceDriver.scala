package org.madnl.iota.api


/**
 * A resource driver manages a resource accessible by HTTP. It must know how to answer to a request made to the
 * resource it is managing. This type encapsulates the application-side of the server.
 */
trait ResourceDriver {

  /**
   * Called when a new request has arrived for this driver
   * @param request The request preamble
   * @param preambleWriter An object with which the response can be written
   * @return A body reader that will be used to read the body. Use BodyReader.Oblivious if you wish to ignore to
   *         the body of the request
   */
  def onRequest(request: Request, preambleWriter: PreambleWriter): BodyReader

}
