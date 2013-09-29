package com.adobe.iota.api


/**
 * Exception caused by errors in configuration
 */
class IotaConfigurationException(message: Option[String], cause: Option[Throwable])
  extends IotaException(message, cause) {

  def this(message: String) = this(Some(message), None)

  def this(cause: Throwable) = this(None, Some(cause))

  def this(message: String, cause: Throwable) = this(Some(message), Some(cause))

}
