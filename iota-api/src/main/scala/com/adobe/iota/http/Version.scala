package com.adobe.iota.http

/**
 * Object specifying a protocol version
 */
private[iota] case class Version(major: Int, minor: Int) {
  override def toString: String = s"$major.$minor"
}

object Version {

  /**
   * Parses the specified string to obtain the version. The string must have the format Major.Minor where
   * Major and Minor are decimal numbers
   * @param str The string to be parsed
   * @return a version object
   * @throws IllegalArgumentException if the string doesn't match the format
   */
  def apply(str: String): Version = {
    str.indexOf('.') match {
      case -1 => throw new IllegalArgumentException("Version string is in invalid format")
      case index => {
        try {
          val majorStr = str.substring(0, index)
          val minorStr = str.substring(index+1)
          Version(majorStr.toInt, minorStr.toInt)
        } catch {
          case e: NumberFormatException => throw new IllegalArgumentException("Invalid number format", e)
        }
      }
    }
  }

}
