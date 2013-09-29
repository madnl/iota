package com.adobe.iota.module.directory

import com.adobe.iota.api._
import com.adobe.iota.http.{Methods, HeaderMap, StatusCodes}
import akka.util.ByteString
import com.adobe.iota.api.Request
import com.google.common.net.HttpHeaders._
import scala.annotation.tailrec
import com.google.common.net.HttpHeaders

/**
 * Driver for serving files from a directory
 */
private[directory] class DirectoryDriver(fs: FileSystemView, indexes: List[String]) extends ResourceDriver {
  import DirectoryDriver._

  def onRequest(request: Request, preambleWriter: PreambleWriter) = {
    if (!isAllowedRequest(request)) {
      preambleWriter.simpleWrite(
        status = StatusCodes.MethodNotAllowed,
        reason = "Method Not Allowed",
        headers = HeaderMap(HttpHeaders.ALLOW -> "GET, HEAD")
      )
    } else {
      val path = request.uri.getPath
      val headersOnly = request.method.equalsIgnoreCase(Methods.Head)
      fs.getMetadata(path) match {
        case None => writeNotFound(preambleWriter)
        case Some(fi: FsFile) => {
          processFile(fi, preambleWriter, headersOnly)
        }
        case Some(fsDir: FsDirectory) => {
          processDirectory(fsDir, preambleWriter)
        }
      }
    }
    BodyReader.Oblivious
  }

  private def isAllowedRequest(request: Request) = {
    val method = request.method
    method.equalsIgnoreCase(Methods.Get) || method.equalsIgnoreCase(Methods.Head)
  }

  private def processFile(fsFile: FsFile, preambleWriter: PreambleWriter, headersOnly: Boolean) {
    val response = buildResponse(fsFile)
    if (headersOnly) {
      preambleWriter.simpleResponseWrite(response)
    } else {
      val bodyWriter = preambleWriter.writePreamble(response)
      fs.processFile(fsFile.absolutePath, processor(bodyWriter))
    }
  }

  private def processDirectory(fsDir: FsDirectory, preambleWriter: PreambleWriter) {
    tryIndex(fsDir.absolutePath) match {
      case None => writeNotFound(preambleWriter)
      case Some(fsFile: FsFile) => {
        preambleWriter.simpleWrite(StatusCodes.Moved, "Moved Permanently",
          headers = HeaderMap(LOCATION -> fsFile.absolutePath)
        )
      }
    }
  }

  private def tryIndex(absPath: String) = {

    @tailrec
    def scan(names: List[String]): Option[FsFile] = names match {
      case Nil => None
      case name :: rest => {
        val path = s"$absPath/$name"
        fs.getMetadata(path) match {
          case Some(fs: FsFile) => Some(fs)
          case _ => scan(rest)
        }
      }
    }

    scan(indexes)
  }

  private def buildResponse(fsFile: FsFile) = Response(
    statusCode = StatusCodes.Ok,
    reasonPhrase = "Found",
    headers = HeaderMap(
      CONTENT_LENGTH -> fsFile.size.toString,
      CONTENT_TYPE -> contentType(fsFile)
    )
  )



  private def writeNotFound(preambleWriter: PreambleWriter) {
    preambleWriter.simpleWrite(status = StatusCodes.NotFound, reason = NotFoundReason, contentUtf8 = NotFoundMessage)
  }

  private def contentType(file: FsFile) = file.charset match {
    case None => file.mimeType
    case Some(name) => s"${file.mimeType}; charset=$name"
  }

  private def processor(bodyWriter: BodyWriter) = new FileProcessor {
    /**
     * Method called when the file was completely read
     */
    def onFileCompleted() {
      bodyWriter.finishBody()
    }

    /**
     * Method call when a file chunk was read and is ready to be processed.
     * @param data the read data
     */
    def onFileChunk(data: ByteString) {
      bodyWriter.writeBodyPart(data)
    }

    /**
     * Method called when an error occurred. After an error occurs the reading process is over, so this should
     * have the same effect as onFileCompleted
     * @param cause the cause of the error
     */
    def onError(cause: Throwable) {
      bodyWriter.finishBody()
      throw cause
    }
  }
}

private[directory] object DirectoryDriver {

  private val NotFoundReason = "Page Not Found"
  private val NotFoundMessage = "No such file exists"

}
