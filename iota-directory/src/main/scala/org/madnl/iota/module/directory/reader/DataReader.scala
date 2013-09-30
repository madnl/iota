package org.madnl.iota.module.directory.reader

import java.io.File
import org.madnl.iota.module.directory.FileProcessor

/**
 * Object of this type implement file reading
 */
private[directory] trait DataReader {

  /**
   * Read the specified file and feed the data into the processor. This method can be implemented
   * asynchronously (i.e it can return before the file was read)
   * @param file The file to be read
   * @param processor The processor that will read the file
   */
  def read(file: File, processor: FileProcessor)

}
