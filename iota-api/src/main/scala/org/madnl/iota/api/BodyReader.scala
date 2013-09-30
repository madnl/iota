package org.madnl.iota.api

import akka.util.ByteString

/**
 * Object provided by an application to read a request body
 */
trait BodyReader {

  def onBodyPart(data: ByteString)

  def onBodyFinished()

}

object BodyReader {

  /**
   * Reader which ignores all body events
   */
  val Oblivious = new BodyReader {
    def onBodyFinished() {}

    def onBodyPart(data: ByteString) {}
  }

}