package com.adobe.iota


/**
 * Various constants and utilities methods which are part of the RFC2616
 */
private[iota] object Protocol {

  /**
   * Line separator for request/response lines and header lines
   */
  val LineSeparator = "\r\n"

  /**
   * The charset used for request/response line & header names and for known header values
   */
  final val PreambleCharset = "US-ASCII"

  /**
   * ASCII SP (space) character
   */
  final val SP = 32

  /**
   * ASCII HT (horizontal tab) character
   */
  final val HT = 9

  /**
   * Divider for name & value in response headers. Complies with the RFC2616 suggestions
   */
  final val CommonHeaderDivider = ": "

  /**
   * Expected header divider in request headers
   */
  final val HeaderDivider = ':'.toInt

  /**
   * Checks if the specified byte char is a control char.
   * @param b the byte to be checked
   * @return true if it's a control char, false otherwise
   */
  def isCtlChar(b: Byte) = b >= 0 && b <= 31

  /**
   * Verifies if the specified byte identifies a character that may appear in a token
   * @param b the byte to be checked
   * @return true if it can be part of a token, false otherwise
   */
  def isTokenChar(b: Byte): Boolean = b > 0 && !isCtlChar(b) && !isSeparator(b)

  /**
   * Checks whether the byte identifies a char that is linear white-space (SP or HT)
   * @param b the char to be checked
   * @return true if it's LWS, false otherwise
   */
  def isLWSChar(b: Byte): Boolean = b == SP || b == HT

  /**
   * Checks if the specified byte identifies a separator character
   * @param b the byte to be checked
   * @return true if it's a separator char, false otherwise
   */
  def isSeparator(b: Byte) = b > 0 && separatorTable(b)

  private val separatorChars = Seq('(', ')', '<', '>', '@', ',', ';', ':', '\\', '\'', '/', '[', ']', '?', '=', '{', '}', ' ' , '\t')

  private val separatorTable = {
    val arr = new Array[Boolean](128)
    for (ch <- separatorChars) {
      arr(ch) = true
    }
    arr
  }

}
