package org.madnl.iota.module.echo

import com.typesafe.config.Config
import org.madnl.iota.api.DriverProvider

/**
 *
 */
class EchoDriverProvider extends DriverProvider {
  /**
   * Given a configuration object, creates a new resource driver
   * @param config The configuration object
   * @return a resource driver
   */
  def create(config: Config) = EchoDriver
}
