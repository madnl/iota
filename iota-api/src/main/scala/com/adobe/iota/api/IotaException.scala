package com.adobe.iota.api

/**
 * Base Iota server exception class
 */
class IotaException(message: Option[String], cause: Option[Throwable])
  extends Exception(message.getOrElse(null), cause.getOrElse(null)) {

  def this(message: String) = this(Some(message), None)

  def this(cause: Throwable) = this(None, Some(cause))

  def this(message: String, cause: Throwable) = this(Some(message), Some(cause))

}
