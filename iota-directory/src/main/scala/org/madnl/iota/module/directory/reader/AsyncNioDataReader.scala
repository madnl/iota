package org.madnl.iota.module.directory.reader

import java.io.File
import org.madnl.iota.module.directory.FileProcessor
import java.nio.channels.{CompletionHandler, AsynchronousFileChannel}
import java.nio.file.StandardOpenOption
import java.nio.ByteBuffer
import akka.util.ByteString

/**
 * Data reader backed by asynchronous file channels introduced in Java 7. It may provide better performance for
 * large files
 */
private[directory] class AsyncNioDataReader(bufferSize: Int) extends DataReader {
  /**
   * Read the specified file and feed the data into the processor. This method can be implemented
   * asynchronously (i.e it can return before the file was read)
   * @param file The file to be read
   * @param processor The processor that will read the file
   */
  def read(file: File, processor: FileProcessor) {
    try {
      val channel = AsynchronousFileChannel.open(file.toPath, StandardOpenOption.READ)
      val buffer = ByteBuffer.allocateDirect(bufferSize)
      val handler = new CompletionHandlerImpl(channel, buffer, processor)
      handler.start()
    } catch {
      case e: Throwable => processor.onError(e)
    }
  }

  private class CompletionHandlerImpl(channel: AsynchronousFileChannel, buffer: ByteBuffer, processor: FileProcessor)
    extends CompletionHandler[Integer, Int] {

    def start() {
      readChannel(0)
    }

    def completed(result: Integer, attachment: Int) {
      val startingPosition = attachment
      val amountRead = result
      if (amountRead > 0) {
        buffer.flip()
        processor.onFileChunk(ByteString(buffer))
        buffer.clear()
        val newStartingPosition = startingPosition + amountRead
        readChannel(newStartingPosition)
      } else {
        processor.onFileCompleted()
        channel.close()
      }
    }

    def failed(exc: Throwable, attachment: Int) {
      processor.onError(exc)
    }

    private def readChannel(startingPosition: Int) {
      channel.read(buffer, startingPosition, startingPosition, this)
    }
  }
}
