package com.adobe.iota.actor

import akka.actor._
import akka.io.{Tcp, TcpReadWriteAdapter, TcpPipelineHandler}
import com.adobe.iota.actor.pipeline.{ConnectionControl, HttpPipeline}
import akka.io.Tcp.Register
import akka.actor.SupervisorStrategy.Stop
import com.adobe.iota.server.Controller
import java.net.InetSocketAddress
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._
import akka.io.TcpPipelineHandler.Management
import com.adobe.iota.actor.pipeline.connection.ConnectionStage

/**
 * The supervisor for the actors handling an active connection
 */
private[actor] class ConnectionSupervisor(connection: ActorRef, controller: Controller, interface: InetSocketAddress)
  extends Actor with ActorLogging {
  import ConnectionSupervisor._
  import context._

  private val init = TcpPipelineHandler.withLogger(log, connectionPipeline)
  private val resourceHandler = actorOf(resourceHandlerProps, "resource")
  private val pipeline = actorOf(pipelineProps, "pipeline")

  private val timeoutDetectionInterval = FiniteDuration(controller.connectionSettings.keepAliveTimeoutMs, MILLISECONDS)
  private val connectionCloseDelay = FiniteDuration(controller.connectionSettings.connectionCloseDelayMs, MILLISECONDS)
  private var requestCount = 0 //counter indicating how many requests were received on this connection

  connection ! Register(pipeline)

  watch(pipeline)
  watch(resourceHandler)

  def receive = {
    case CloseConnection => {
      //closing the connection with a delay
      system.scheduler.scheduleOnce(connectionCloseDelay, connection, Tcp.Close)
    }
    case NewRequest => {
      //on new request increase the counter and schedule a message to check if the keep alive timeout was reached
      requestCount = requestCount + 1
      system.scheduler.scheduleOnce(timeoutDetectionInterval, self, NewRequestTimeout(requestCount))
    }
    case NewRequestTimeout(requestIndex) if requestIndex == requestCount => {
      //no new connection in the last `timeoutDetectionInterval` ms. We can signal to the pipeline to wrap-up the responses and close
      pipeline ! Management(ConnectionStage.EnterClosingMode)
    }
    case Terminated(`pipeline` | `resourceHandler`) => {
      stop(self)
    }
    case Tcp.Closed => {
      stop(self)
    }
  }

  override val supervisorStrategy = AllForOneStrategy(loggingEnabled = true) {
    case _ => Stop //supervision policy is pretty blunt - on exception we just close down the entire connection
  }

  private def connectionPipeline = HttpPipeline(mkConnectionControl()) >> new TcpReadWriteAdapter

  private def resourceHandlerProps =
    ResourceHandler(controller, interface, init).withDispatcher("akka.custom-dispatchers.driver-dispatcher")

  private def pipelineProps =
    TcpPipelineHandler.props(init, connection, resourceHandler).withDispatcher("akka.custom-dispatchers.pipeline-dispatcher")

  private def mkConnectionControl() = new ConnectionControl {
    def notifyOfNewRequest() {
      self ! NewRequest
    }

    def close() {
      self ! CloseConnection
    }

    def keepAliveTimeoutMs = controller.connectionSettings.keepAliveTimeoutMs
  }
}

private[actor] object ConnectionSupervisor {

  def apply(connection: ActorRef, controller: Controller, interface: InetSocketAddress): Props =
    Props(new ConnectionSupervisor(connection, controller, interface))

  /**
   * Message received from the pipeline indicating that the connection is ready to be closed
   */
  private case object CloseConnection

  /**
   * Message received when a new request has arrived on the connection
   */
  private case object NewRequest

  /**
   * Message received indicating that the supervisor should check if the connection should be closed
   */
  private case class NewRequestTimeout(lastReceived: Int)
}