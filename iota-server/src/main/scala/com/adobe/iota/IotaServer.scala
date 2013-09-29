package com.adobe.iota

import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import com.adobe.iota.actor.Manager
import scala.concurrent.{Await, Future}
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import com.adobe.iota.server.Controller
import com.typesafe.config.ConfigFactory
import java.io.File
import com.adobe.iota.config.ServerConfig
import scala.concurrent.duration.Duration
import com.adobe.iota.api.IotaException

/**
 * Class used for web server administration
 */
class IotaServer(manager: ActorRef, ownedSystem: Option[ActorSystem]) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  /**
   * Stops the web server by shutting down all the listening ports
   * @return a future to be completed when the operation finishes
   */
  def stop(): Future[Unit] = {
    (manager ? Manager.ShutdownAll).andThen {
      case _ => ownedSystem.foreach(_.shutdown())
    }.mapTo[Unit]
  }


}

object IotaServer {

  /**
   * Create a web server within the specified actor system
   * @param actorSystem the actor system that will contain the server
   * @return a web server instance
   */
  def apply(actorSystem: ActorSystem, controller: Controller): IotaServer = create(actorSystem, owned = false, controller)

  /**
   * Creates an actor system and a web server within it. The lifecycle of the actor system is bound
   * to the lifecycle of the server
   * @return A web server instance
   */
  def apply(controller: Controller): IotaServer = create(ActorSystem("iota-ws"), owned = true, controller)

  private def create(actorSystem: ActorSystem, owned: Boolean, controller: Controller): IotaServer = {
    val manager = actorSystem.actorOf(Manager(controller), "iota-manager")
    new IotaServer(manager, if (owned) Some(actorSystem) else None)
  }

  /**
   * Running the server as stand-alone
   * @param args Application arguments
   */
  def main(args: Array[String]) {
    val referenceConfig = ConfigFactory.load()
    val serverConfPath = referenceConfig.getString("iota.configFile")
    val serverConfFile = new File(serverConfPath)
    if (!serverConfFile.exists()) {
      throw new IotaException(s"Configuration file $serverConfFile does not exist")
    }
    val instanceConf = ConfigFactory.parseFile(serverConfFile)
    val conf = instanceConf.getConfig("iota").withFallback(referenceConfig.getConfig("iota"))
    val serverConf = ServerConfig.fromConfig(conf)
    val controller = Controller.fromConfig(serverConf)
    val server = IotaServer(controller)
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run() {
        Await.ready(server.stop(), atMost = Duration(5, TimeUnit.SECONDS))
      }
    }))
  }

}
