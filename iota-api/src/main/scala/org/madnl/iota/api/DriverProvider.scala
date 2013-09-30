package org.madnl.iota.api

import com.typesafe.config.Config

/**
 * Class that acts as a factory for drivers
 */
trait DriverProvider {

  /**
   * Given a configuration object, creates a new resource driver
   * @param config The configuration object
   * @return a resource driver
   */
  def create(config: Config): ResourceDriver

}
