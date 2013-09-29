package com.adobe.iota.actor.pipeline.segment

import akka.util.ByteString
import scala.collection.mutable.ArrayBuffer

/**
 * Simple abstraction used to parse http requests into useful segments of data. It implements the HTTP parsing
 * as a state machine, where each state tries to consume a chunk of bytes from a buffer filled with client data
 */
abstract class EventMachine[T] {

  type State = () => Unit

  private var currentState: State = initially
  private val eventBuffer = ArrayBuffer[T]()
  private val dataBuffer = new DataBuffer
  private var active = true

  /**
   * Feed data into the machine's buffer
   * @param data the data to be fed
   * @return a series of events resulting from the new incoming data
   */
  def feed(data: ByteString): Iterable[T] = {
    eventBuffer.clear()
    dataBuffer.feed(data)
    cycle()
    eventBuffer
  }

  /**
   * Transition to a new state
   * @param state the new state of the event machine
   */
  protected[EventMachine] def become(state: State) {
    currentState = state
    active = true
  }

  /**
   * Emit a new event
   * @param event the event
   */
  protected def emit(event: T) {
    eventBuffer += event
  }

  /**
   * Consume from the buffer until the `separator` is met. If no separator exists, return nothing
   * @param separator to searched separator
   * @return an optional byte string
   */
  final protected def consumeUntil(separator: ByteString) = dataBuffer.consumeUntil(separator)

  /**
   * Consume at `size` bytes from the buffer if there are `size` bytes available
   * @param size the size of the buffer
   * @return an optional byte string
   */
  final protected def consume(size: Int) = dataBuffer.consume(size)

  /**
   * Consume at most `size` bytes from the buffer
   * @param size the maximum size of the result
   * @return a byte string
   */
  final protected def consumeAtMost(size: Int): ByteString = dataBuffer.consumeAtMost(size)

  /**
   * Specifies the initial state
   */
  protected def initially: State

  /**
   * The transition cycle implementation
   */
  private def cycle() {
    active = true
    while(active) {
      active = false
      currentState()
    }
  }

}
