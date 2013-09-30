package org.madnl.iota.server

import java.io.File
import org.madnl.iota.api.Request
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import ch.qos.logback.core.{OutputStreamAppender, ConsoleAppender, FileAppender}
import ch.qos.logback.classic.spi.ILoggingEvent

/**
 * Simple implementation for a logger
 */
class SimpleLogger(logger: Logger) extends RequestLogger {

  def log(request: Request) {
    logger.info(s"${request.method} ${request.uri}")
  }

}

object SimpleLogger {

  def fromFile(file: File): SimpleLogger = new SimpleLogger(mkLogger(fileAppender(file)))

  def console: SimpleLogger = new SimpleLogger(mkLogger(consoleAppender))

  private def mkLogger(appender: OutputStreamAppender[ILoggingEvent]) = {
    val logger = LoggerFactory.getLogger("http-logger").asInstanceOf[Logger]
    logger.addAppender(appender)
    logger.setLevel(Level.DEBUG)
    logger.setAdditive(false)
    logger
  }

  private lazy val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  private def patternLayoutEncoder = {
    val pattern = new PatternLayoutEncoder
    pattern.setPattern("%date %msg%n")
    pattern.setContext(context)
    pattern.start()
    pattern
  }

  private def fileAppender(file: File) = {
    val fileAppender = new FileAppender[ILoggingEvent]()
    fileAppender.setFile(file.getCanonicalPath)
    fileAppender.setEncoder(patternLayoutEncoder)
    fileAppender.setContext(context)
    fileAppender.start()
    fileAppender
  }

  private def consoleAppender = {
    val appender = new ConsoleAppender[ILoggingEvent]
    appender.setEncoder(patternLayoutEncoder)
    appender.setContext(context)
    appender.start()
    appender
  }

}