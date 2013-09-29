package com.adobe.iota.module.directory.reader

import java.io.{FileInputStream, File}
import com.adobe.iota.module.directory.FileProcessor
import resource._
import java.nio.ByteBuffer
import akka.util.ByteString

/**
 * Data reader backed by Java NIO file channels
 */
private[directory] class NioDataReader(bufferSize: Int) extends DataReader {
  /**
   * Read the specified file and feed the data into the processor. This method can be implemented
   * asynchronously (i.e it can return before the file was read)
   * @param file The file to be read
   * @param processor The processor that will read the file
   */
  def read(file: File, processor: FileProcessor) {
    try {
      managed(new FileInputStream(file)) acquireAndGet { input =>
        managed(input.getChannel) acquireAndGet { fileChannel =>
          val buffer = ByteBuffer.allocateDirect(bufferSize)
          var continue = true
          while (continue) {
            val amountRead = fileChannel.read(buffer)
            buffer.flip()
            if (amountRead > 0) {
              processor.onFileChunk(ByteString(buffer))
              buffer.clear()
            } else {
              continue = false
            }
          }
          processor.onFileCompleted()
        }
      }
    } catch {
      case e: Exception => processor.onError(e)
    }
  }
}
