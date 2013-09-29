package com.adobe.iota.actor.pipeline

import akka.event.LoggingAdapter

/**
 *
 */
class LoggingAdapterStub extends LoggingAdapter {

  var enabled = true

  def isErrorEnabled = enabled

  def isWarningEnabled = enabled

  def isInfoEnabled = enabled

  def isDebugEnabled = enabled

  protected def notifyError(message: String) {
    notifyMessage("ERROR", message)
  }

  protected def notifyError(cause: Throwable, message: String) {
    notifyError(s"$message:\n${cause.getStackTraceString}")
  }

  protected def notifyWarning(message: String) {
    notifyMessage("WARN", message)
  }

  protected def notifyInfo(message: String) {
    notifyMessage("INFO", message)
  }

  protected def notifyDebug(message: String) {
    notifyMessage("DEBUG", message)
  }

  private def notifyMessage(level: String, message: String) = {
    if (enabled) {
      println(s"[$level]: $message")
    }
  }
}
