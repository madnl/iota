package com.adobe.iota.module.directory

import java.io.{FileNotFoundException, File}

/**
 * A view of a file system, allowing for querying of file metadata and reading
 */
trait FileSystemView {

  /**
   * Tries to obtain the metadata for a file
   * @param path the path of the file
   * @return some file data structure or none if no such file exists
   */
  def getMetadata(path: String): Option[FsEntity]

  /**
   * Processes a given file, possibly asynchronously
   * @param path the path to the file
   * @param processor the processor for the file
   * @throws FileNotFoundException if there is no file indicated by the given path
   */
  def processFile(path: String, processor: FileProcessor)

}
