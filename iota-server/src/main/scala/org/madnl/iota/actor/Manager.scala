package org.madnl.iota.actor

import akka.actor._
import akka.actor.ActorDSL._
import java.net.InetSocketAddress
import akka.actor.Status.Failure
import org.madnl.iota.api.{IotaException, ResourceDriver, ResourcePattern}
import akka.io.{Tcp, IO}
import akka.io.Tcp.{CommandFailed, Bound, Bind}
import org.madnl.iota.server.Controller

/**
 * Highest actor in the server hierarchy. Manages the listener actors.
 */
private[actor] class Manager(controller: Controller) extends Actor with ActorLogging {
  import context.{system => _, _}
  import Manager._

  private var listeners = List[ActorRef]()

  for (interface <- controller.listeningInterfaces) { createListener(interface) }

  def receive = {
    case ShutdownAll => {
      listeners.foreach(stopListener)
      sender ! OperationCompleted
    }
    case AddListener(_, listener) => {
      listeners = listener :: listeners
    }
  }

  //stop a specified listener
  private def stopListener(ref: ActorRef) {
    ref ! PoisonPill
  }

  private def createListener(address: InetSocketAddress) = {
    val props = SocketListener(controller)
    val listener = actorOf(props, s"listener-${listeners.size}")
    bindToSocket(address, listener, self)
  }

  private def bindToSocket(address: InetSocketAddress, listener: ActorRef, observer: ActorRef) = {
    val manager = self
    //Creating a temporary actor that will open the listening socket, bind the listener to it
    //and announce everyone involved about the success/failure of this process and afterwords stop itself
    actor(new Act {
      import context._

      IO(Tcp) ! Bind(listener, address)

      become {
        case Bound(adr) => {
          log.info(s"Connection to address [$adr] established. Accepting connections")
          manager ! AddListener(adr, listener)
          observer ! OperationCompleted
          stop(self)
        }
        case CommandFailed(b: Bind) => {
          log.error(s"Could not bind to address ${b.localAddress}")
          observer ! Failure(new IotaException(s"Could not bind to address ${b.localAddress}"))
          stop(listener)
          stop(self)
        }
      }
    })
  }
}


private[iota] object Manager {

  /**
   * Provides the props to create a manager actor
   * @return a manager actor props object
   */
  def apply(controller: Controller): Props = Props(new Manager(controller))

  /**
   * Command sent to the manager to open the specified local address and listen to it for connections
   * @param inetSocketAddress the address to be listened on
   */
  case class ListenOn(inetSocketAddress: InetSocketAddress)

  /**
   * Command sent to the manager to close all the listening ports
   */
  case object ShutdownAll

  /**
   * Command sent to the manager to register a driver for the specified resource pattern
   * @param pattern The target resource pattern
   * @param driver The driver that will handle it
   */
  case class RegisterResource(pattern: ResourcePattern, driver: ResourceDriver)

  /**
   * Response sent by the manager to indicate that a command was successfully completed
   */
  case object OperationCompleted


  private case class AddListener(address: InetSocketAddress, listener: ActorRef)

}
