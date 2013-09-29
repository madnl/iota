package com.adobe.iota.module.directory

import akka.util.ByteString

/**
 * A file processor is a component which consumes the data of a file
 */
trait FileProcessor {

  /**
   * Method call when a file chunk was read and is ready to be processed.
   * @param data the read data
   */
  def onFileChunk(data: ByteString)

  /**
   * Method called when the file was completely read
   */
  def onFileCompleted()

  /**
   * Method called when an error occurred. After an error occurs the reading process is over, so this should
   * have the same effect as onFileCompleted
   * @param cause the cause of the error
   */
  def onError(cause: Throwable)

}
