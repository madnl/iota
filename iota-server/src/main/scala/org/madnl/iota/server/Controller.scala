package org.madnl.iota.server

import java.net.InetSocketAddress
import org.madnl.iota.config.{ConnectionSettings, LogOutputLocation, ServerConfig}
import java.io.{FileWriter, BufferedWriter, File}

/**
 *
 */
case class Controller(listeningInterfaces: Seq[InetSocketAddress],
                      resourceResolver: ResourceResolver,
                      logger: RequestLogger,
                      connectionSettings: ConnectionSettings)

object Controller {

  def fromConfig(serverConf: ServerConfig): Controller = {
    Controller(
      listeningInterfaces = serverConf.interfaces,
      resourceResolver = ResourceResolver.fromSites(serverConf.sites),
      logger = createLogger(serverConf.logOutputLocation),
      connectionSettings = serverConf.connectionSettings
    )
  }

  private def createLogger(location: LogOutputLocation) = location match {
    case LogOutputLocation.File(path) => {
      val file = new File(path)
      SimpleLogger.fromFile(file)
    }
    case LogOutputLocation.Stdout => SimpleLogger.console
    case LogOutputLocation.Null => NullLogger
  }

}