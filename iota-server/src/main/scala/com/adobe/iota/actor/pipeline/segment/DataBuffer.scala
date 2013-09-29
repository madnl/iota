package com.adobe.iota.actor.pipeline.segment

import akka.util.ByteString

/**
 * Raw byte string buffer
 */
class DataBuffer {

  private var buffer = ByteString()

  /**
   * Append the provided data to the buffer
   * @param data the data to be added
   */
  def feed(data: ByteString) {
    if (buffer.isEmpty) { // equivalent with doing just "buffer++data" but we avoid object allocation in this way
      buffer = data
    } else {
      buffer = buffer ++ data
    }
  }

  /**
   * Consume from the buffer until the given separator is met. If there is no such separator in the buffer, return
   * nothing
   * @param separator the separator to be found
   * @return an optional byte string representing the portion until the separator
   */
  def consumeUntil(separator: ByteString): Option[ByteString] = {
    buffer.indexOfSlice(separator) match {
      case -1 => None
      case index => {
        val line = buffer.take(index)
        buffer = buffer.drop(index + separator.size)
        Some(line)
      }
    }
  }

  /**
   * Consume exactly chunkSize bytes from the buffer. If chunkSize bytes are not available return nothing
   * @param chunkSize the chunk size
   * @return an optional byte string with size exactly chunk size
   */
  def consume(chunkSize: Int): Option[ByteString] = {
    if (buffer.size >= chunkSize) {
      val (chunk, remaining) = buffer.splitAt(chunkSize)
      buffer = remaining
      Some(chunk)
    } else {
      None
    }
  }

  /**
   * Consume at most `size` bytes from the buffer.
   * @param size The size of the resulting byte string
   * @return a byte string with at most `size` bytes
   */
  def consumeAtMost(size: Int): ByteString = {
    require(size >= 0)
    if (buffer.size > size) {
      val (packet, remaining) = buffer.splitAt(size)
      buffer = remaining
      packet
    } else {
      val result = buffer
      buffer = ByteString()
      result
    }
  }

  /**
   * The current content of the buffer
   * @return a byte string with the buffer's content
   */
  def leftOver: ByteString = buffer

}
