package com.adobe.iota.module.directory

/**
 * Entity in a file system
 */
abstract sealed class FsEntity

/**
 * A file system file
 * @param absolutePath the absolute path of the file, as seen from the view
 * @param size the size of the file
 * @param mimeType the mime type
 * @param charset the charset of the file, if applicable and available
 */
case class FsFile(absolutePath: String, size: Long, mimeType: String, charset: Option[String]) extends FsEntity

/**
 * A file system directory
 * @param absolutePath the absolute path of the directory as seen from the view
 */
case class FsDirectory(absolutePath: String) extends FsEntity
