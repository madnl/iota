package org.madnl.iota.actor.pipeline


/**
 * Object used to communicate the state of the requests coming on the connection and to close the connection
 */
trait ConnectionControl {

  /**
   * Closes the connection
   */
  def close()

  /**
   * Notifies that a new request has been received
   */
  def notifyOfNewRequest()

  /**
   * The timeout of the connection keep-alive
   * @return an integer in milliseconds
   */
  def keepAliveTimeoutMs: Int

}
