package com.adobe.iota.config

import com.typesafe.config._
import com.adobe.iota.api._
import java.lang.reflect.InvocationTargetException
import scala.Some

/**
 * A module contains information about how to instantiate a resource driver
 */
case class Module(pattern: ResourcePattern, provider: DriverProvider, config: Config) {

  def newDriver(): ResourceDriver = provider.create(config)

}

object Module {

  def fromConfig(originalConfig: Config, baseConfigs: BaseConfigs): Module = {
    try {
      val config = originalConfig.withFallback(baseConfigs.defaultModule)
      val knownDriversConfig = baseConfigs.knownDrivers
      val pattern = readMatchPattern(config)
      val (provider, driverConfig) = readProvider(config, knownDriversConfig)
      Module(pattern, provider, driverConfig)
    } catch {
      case e: ConfigException => throw new IotaConfigurationException("Module configuration error", e)
    }
  }

  private val matchMapping = List[(String, String => ResourcePattern)](
    "match.exact"   -> { (s) => new ExactMatch(s) },
    "match.prefix"  -> { (s) => new WithPathPrefix(s) },
    "match.regex"   -> { (s) => new PathRegex(s) }
  )

  private def readMatchPattern(config: Config) = {
    matchMapping collectFirst {
      case (key, maker) if config.hasPath(key) => maker(config.getString(key))
    } getOrElse Anything
  }

  private def readProvider(config: Config, knownDriversConfig: Config): (DriverProvider, Config) = {
    val fromClass = for(cls <- tryGetString(config, "driver.class")) yield (classResourceProvider(cls), config.getConfig("config"))
    fromClass.getOrElse(knownResourceProvider(config, knownDriversConfig))
  }

  private def knownResourceProvider(config: Config, knownDriversConfig: Config): (DriverProvider, Config) = {
    val driver = config.getString("driver")
    if (knownDriversConfig.hasPath(driver)) {
      val clsName = knownDriversConfig.getConfig(driver).getString("class")
      (classResourceProvider(clsName), config.getConfig("config").withFallback(knownDriversConfig.getConfig(s"$driver.config")))
    } else {
      throw new IotaConfigurationException(s"No such driver was configured: $driver")
    }
  }

  private def classResourceProvider(clsName: String): DriverProvider = {
    try {
      val clazz = Class.forName(clsName)
      val resourceProviderClass = classOf[DriverProvider]
      if (!resourceProviderClass.isAssignableFrom(clazz))
        throw new IotaConfigurationException(s"Class $clsName is not a subtype of ${resourceProviderClass.getCanonicalName}")
      else {
        val constructor = clazz getConstructor()
        constructor.newInstance().asInstanceOf[DriverProvider]
      }
    } catch {
      case e: ClassNotFoundException =>
        throw new IotaConfigurationException(s"No such resource provider class exists: $clsName")
      case e: InvocationTargetException =>
        throw new IotaConfigurationException(s"Exception when invoking resource provider constructor", e)
      case e: NoSuchMethodException =>
        throw new IotaConfigurationException(s"Resource provider $clsName has no nullary constructor")
    }

  }

  private def tryGetString(config: Config, path: String): Option[String] = {
    if (config.hasPath(path))
      Some(config.getString(path))
    else
      None
  }



}
