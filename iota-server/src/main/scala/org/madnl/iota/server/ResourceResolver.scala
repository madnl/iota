package org.madnl.iota.server

import java.net.URI
import org.madnl.iota.api.{Anything, ConstantResourceDriver, ResourcePattern, ResourceDriver}
import scala.collection.immutable.HashMap
import org.madnl.iota.config.{Module, Site}
import com.google.common.net.HostAndPort

/**
 * The resource resolver associates resource patterns with drivers. It can provide, for a given URI, the
 * driver that must handle that request, or a default one if no drivers was registered for the URI
 */
class ResourceResolver private(siteMapping: Map[HostAndPort, ResourceResolver.DriverMapping]) {

  def resolve(host: HostAndPort, uri: URI): ResourceDriver = {
    val resources = siteMapping(host)
    val driverOpt = resources.mappings collectFirst {
      case (pattern, driver) if pattern.matchURI(uri) => driver
    }
    driverOpt.getOrElse(ConstantResourceDriver.NotFound)
  }

}


object ResourceResolver {

  def fromSites(sites: Seq[Site]): ResourceResolver = {
    val entries = for (site <- sites) yield (site.hostAndPort, buildDrivers(site.modules))
    val mapping = HashMap(entries: _*)
    val default = sites.find(_.isDefault) match {
      case None       => DriverMapping(Seq(Anything -> ConstantResourceDriver.NotFound))
      case Some(site) => mapping(site.hostAndPort)
    }
    val finalMapping = mapping.withDefaultValue(default)
    new ResourceResolver(finalMapping)
  }

  private def buildDrivers(modules: Seq[Module]) =
    DriverMapping(for (module <- modules) yield (module.pattern, module.newDriver()))


  case class DriverMapping(mappings: Seq[(ResourcePattern, ResourceDriver)])
}