package org.madnl.iota.module.directory

import java.io.File
import org.madnl.iota.module.directory.reader.DataReader

/**
 * File system view which gives access to files under a specified directory
 */
private[directory] class DirectoryFileSystemView(baseDirectory: File, dataReader: DataReader, mimeTypeMapping: Map[String, String]) extends FileSystemView {
  import DirectoryFileSystemView._

  require(baseDirectory.isDirectory, "Specified path in directory driver is not a valid directory path")

  private val canonicalPath = baseDirectory.getCanonicalPath

  def getMetadata(path: String) = {
    for (file <- getFile(path)) yield {
      if (file.isFile) {
        val (mimeType, charset) = findMimeType(file.getName)
        FsFile(file.getCanonicalPath.stripPrefix(canonicalPath), file.length(), mimeType, charset)
      } else {
        FsDirectory(file.getCanonicalPath.stripPrefix(canonicalPath))
      }
    }
  }


  def processFile(path: String, processor: FileProcessor) {
    val file = getFile(path).get
    dataReader.read(file, processor)
  }

  private def getFile(path: String) = {
    val f = new File(baseDirectory, path)
    if (f.exists && f.getCanonicalPath.startsWith(canonicalPath))
      Some(f)
    else
      None
  }

  def findMimeType(fileName: String) = {
    val extension = fileName.lastIndexOf('.') match {
      case -1 => ""
      case index => fileName.substring(index + 1).toLowerCase
    }
    val charset = if (TextFiles.contains(extension)) Some("utf-8") else None
    (mimeTypeMapping(extension), charset)
  }


}

private[directory] object DirectoryFileSystemView {

  private val TextFiles = Set("html", "css", "js")

}