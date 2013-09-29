package com.adobe.iota.actor.pipeline

import com.adobe.iota.api.IotaException

/**
 * Exception to be thrown when the server cannot handle certain protocol features
 */
class UnsupportedProtocolFeature(message: String) extends IotaException(message)
