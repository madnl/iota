package org.madnl.iota.config

/**
 * Connection-specific settings
 * @param keepAliveTimeoutMs The duration after which the connection is to be closed if no requests arrive
 * @param connectionCloseDelayMs The delay before closing a connection
 */
case class ConnectionSettings(keepAliveTimeoutMs: Int, connectionCloseDelayMs: Int)
