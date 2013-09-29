package com.adobe.iota.actor

import akka.actor.{Props, ActorLogging, Actor}
import akka.io.Tcp.Connected
import com.adobe.iota.server.Controller

/**
 * Actor responsible with accepting incoming connections and dispatching those connections to handling
 * actors
 */
private[actor] class SocketListener(controller: Controller)
  extends Actor with ActorLogging {
  import context._

  private var connectionCounter = 0

  def receive = {
    case Connected(remote, address) => {
      connectionCounter += 1
      log.debug(s"Incoming connection from $remote to $address")
      actorOf(ConnectionSupervisor(sender, controller, address), s"con-super-$connectionCounter")
    }
  }

}

private[actor] object SocketListener {

  def apply(controller: Controller): Props =
    Props(new SocketListener(controller))

}
