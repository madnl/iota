package org.madnl.iota

import org.madnl.iota.http.Version

/**
 * Various parameters specific to the server
 */
private[iota] object ServerParameters {

  /**
   * The version of the protocol implemented by this server
   */
  final val ProtocolVersion = Version(1, 1)

}
