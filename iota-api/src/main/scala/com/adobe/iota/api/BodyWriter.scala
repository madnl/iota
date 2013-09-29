package com.adobe.iota.api

import akka.util.ByteString

/**
 * This body is provided to an application for a new incoming request to write the response body after writing the
 * response preamble.
 */
trait BodyWriter {

  /**
   * Writes a part of the body's response. This method may work asynchronously (i.e. it doesn't guarantee that
   * the body part is actually written after the call has finished)
   * @param data The data to be written
   * @return this object
   * @throws IllegalStateException If the finish body method has been previously called
   */
  def writeBodyPart(data: ByteString): this.type

  /**
   * Indicates that the body has been completed.
   * @throws IllegalStateException If finish body has been called already
   */
  def finishBody()

}
