package org.madnl.iota.config

/**
 * Location for the log file
 */
abstract sealed class LogOutputLocation

object LogOutputLocation {

  case object Stdout extends LogOutputLocation
  case class File(path: String) extends LogOutputLocation
  case object Null extends LogOutputLocation

}
