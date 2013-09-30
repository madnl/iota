package org.madnl.iota.actor.pipeline

import org.madnl.iota.api.IotaException

/**
 * Exception to be thrown when the server cannot handle certain protocol features
 */
class UnsupportedProtocolFeature(message: String) extends IotaException(message)
