package org.madnl.iota.module.directory.reader

import java.io.{FileInputStream, BufferedInputStream, File}
import org.madnl.iota.module.directory.FileProcessor
import akka.util.ByteString

/**
 * Data reader implemented using input streams
 */
private[directory] class InputStreamDataReader(bufferSize: Int) extends DataReader {
  /**
   * Read the specified file and feed the data into the processor. This method can be implemented
   * asynchronously (i.e it can return before the file was read)
   * @param file The file to be read
   * @param processor The processor that will read the file
   */
  def read(file: File, processor: FileProcessor) {
    try {
      for (in <- resource.managed(new BufferedInputStream(new FileInputStream(file)))) {
        val buffer = new Array[Byte](bufferSize)
        var continue = true
        while (continue) {
          val amountRead = in.read(buffer)
          if (amountRead > 0) {
            processor.onFileChunk(ByteString(buffer).take(amountRead))
          } else {
            continue = false
          }
        }
        processor.onFileCompleted()
      }
    } catch {
      case e: Exception => processor.onError(e)
    }
  }
}
