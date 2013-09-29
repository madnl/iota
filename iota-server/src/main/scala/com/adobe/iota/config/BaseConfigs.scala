package com.adobe.iota.config

import com.typesafe.config.Config

/**
 * Default configurations for various server entities
 */
case class BaseConfigs(defaultSite: Config,
                       defaultModule: Config,
                       knownDrivers: Config)
