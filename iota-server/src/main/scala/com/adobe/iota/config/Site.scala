package com.adobe.iota.config

import com.typesafe.config.Config
import scala.collection.JavaConversions._
import com.google.common.net.HostAndPort

/**
 * Configuration for a site
 */
case class Site(name: String, hostAndPort: HostAndPort, isDefault: Boolean, modules: Seq[Module])

object Site {

  def fromConfig(name: String, config: Config, baseConfigs: BaseConfigs): Site = {
    val conf = config.withFallback(baseConfigs.defaultSite)
    val host = HostAndPort.fromString(conf.getString("host")).withDefaultPort(80)
    val modules = for (moduleConf <- conf.getConfigList("modules")) yield Module.fromConfig(moduleConf, baseConfigs)
    val isDefault = conf.getBoolean("default")
    Site(name, host, isDefault, modules)
  }

}
