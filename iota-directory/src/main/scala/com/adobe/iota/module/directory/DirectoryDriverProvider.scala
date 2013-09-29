package com.adobe.iota.module.directory

import com.typesafe.config.Config
import java.io.File
import com.adobe.iota.api.{IotaConfigurationException, DriverProvider}
import com.adobe.iota.module.directory.reader.{AsyncNioDataReader, NioDataReader, InputStreamDataReader}
import scala.collection.JavaConversions._

/**
 * Factory for the directory driver
 */
class DirectoryDriverProvider extends DriverProvider {

  /**
   * Given a configuration object, creates a new resource driver
   * @param config The configuration object
   * @return a resource driver
   */
  def create(config: Config) = {


    val path = config.getString("path")
    val bufferSize = config.getInt("buffer-size")
    val dataReader = getDataReader(config, bufferSize)
    val mapping = readMimeTypeMappings(config)
    val view = new DirectoryFileSystemView(new File(path), dataReader, mapping)
    val indexes = config.getStringList("index").toList
    new DirectoryDriver(view, indexes)
  }

  private def getDataReader(config: Config, bufferSize: Int) = config.getString("data-reader") match {
    case "classic"    => new InputStreamDataReader(bufferSize)
    case "nio"        => new NioDataReader(bufferSize)
    case "async-nio"  => new AsyncNioDataReader(bufferSize)
    case unknown      => throw new IotaConfigurationException(s"Unknown data reader in config: [$unknown]")
  }

  private def readMimeTypeMappings(config: Config) = {
    val xs = for (entry <- config.getObject("mime-type-mapping").entrySet()) yield
      (entry.getKey, entry.getValue.unwrapped().asInstanceOf[String])
    Map(xs.toList: _*).withDefaultValue(config.getString("default-mime-type"))
  }
}
