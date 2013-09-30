package org.madnl.iota.config

import com.typesafe.config.Config
import scala.collection.JavaConversions._
import java.net.{InetAddress, InetSocketAddress}
import com.google.common.net.{InetAddresses, HostAndPort}
import java.io.File
import org.madnl.iota.api.IotaConfigurationException

/**
 * Configuration used to instantiate a server
 */
case class ServerConfig(interfaces: Seq[InetSocketAddress], logOutputLocation: LogOutputLocation, connectionSettings: ConnectionSettings, sites: Seq[Site])

object ServerConfig {

  def fromConfig(config: Config): ServerConfig = {
    val interfaces = getInterfaces(config)
    val baseConfigs = readBaseConfigs(config)
    val logLocation = readLogLocation(config.getConfig("log"))
    val sitesConfig = config.getConfig("sites")
    val sites = {
      for (name <- sitesConfig.root().keySet().toList)
      yield {
        Site.fromConfig(name, sitesConfig.getConfig(name), baseConfigs)
      }
    }
    val connectionSettings = getConnectionSettings(config)
    ServerConfig(interfaces, logLocation, connectionSettings, sites)
  }

  private def getConnectionSettings(config: Config): ConnectionSettings = {
    ConnectionSettings(
      keepAliveTimeoutMs = config.getMilliseconds("connection.keep-alive-timeout").toInt,
      connectionCloseDelayMs = config.getMilliseconds("connection.closing-delay").toInt
    )
  }

  private def getInterfaces(config: Config) = {
    for (str <- config.getStringList("listen")) yield {
      val hostAndPort = HostAndPort.fromString(str)
      val host = hostAndPort.getHostText
      val port = hostAndPort.getPortOrDefault(80)
      if (host == "*")
        new InetSocketAddress(port)
      else
        new InetSocketAddress(InetAddresses.forString(host), port)
    }
  }

  private def readBaseConfigs(config: Config) = {
    BaseConfigs(
      defaultSite = config.getConfig("defaults.site"),
      defaultModule = config.getConfig("defaults.module"),
      knownDrivers = config.getConfig("drivers")
    )
  }

  private def readLogLocation(logConfig: Config): LogOutputLocation = {
    if (logConfig.hasPath("file")) {
      LogOutputLocation.File(logConfig.getString("file"))
    } else {
      logConfig.getString("device") match {
        case "stdout" => LogOutputLocation.Stdout
        case "none" =>   LogOutputLocation.Null
        case _ => throw new IotaConfigurationException("Invalid logging device")
      }
    }
  }

}
