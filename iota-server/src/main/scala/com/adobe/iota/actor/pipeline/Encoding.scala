package com.adobe.iota.actor.pipeline

import akka.util.ByteString
import com.adobe.iota.Protocol

/**
 * Charset encoding/decoding methods specific to the HTTP 1.1 protocol
 */
private[pipeline] object Encoding {

  /**
   * Encode the specified string to be used in a HTTP message preamble (response line, header)
   * @param s the string to be encoded
   * @return a byte string representation
   */
  def preambleEncoded(s: String) = ByteString(s, Protocol.PreambleCharset)

  /**
   * Decode the given byte-string by interpreting it as part of a HTTP message preamble (request line, header)
   * @param bs the byte string to be read
   * @return the resulting string
   */
  def preambleDecode(bs: ByteString) = bs.decodeString(Protocol.PreambleCharset)

}
