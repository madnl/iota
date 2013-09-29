package com.adobe.iota.actor.pipeline

import akka.io.PipePair

/**
 * Trait which allows modelling a pipeline as a state machine
 */
trait StateMachine[CmdAbove, CmdBelow, EvtAbove, EvtBelow] extends PipePair[CmdAbove, CmdBelow, EvtAbove, EvtBelow] {

  /**
   * Unidirectional pipe
   * @tparam T The type of messages handled and produced
   */
  protected[StateMachine] trait UniPipe[T] extends Function[T, Iterable[Result]] {
    final def apply(cmd: T) = handle(cmd)
    def handle: PartialFunction[T, Iterable[Result]]
  }

  type CommandPipe = UniPipe[CmdAbove]
  type EventPipe = UniPipe[EvtBelow]

  //current command pipe
  private var commandPipe: CommandPipe = new CommandPipe { val handle: PartialFunction[CmdAbove, Iterable[Result]] = {case cmd => throw new IllegalStateException("No command pipe set") }}

  //current event pipe
  private var eventPipe: EventPipe = new EventPipe { val handle: PartialFunction[EvtBelow, Iterable[Result]] = {case evt => throw new IllegalStateException("No event pipe set") }}

  //factory object used to create command pipes
  object CommandPipe {
    def apply(handler: PartialFunction[CmdAbove, Iterable[Result]]): CommandPipe = new CommandPipe {
      val handle = handler
    }
  }

  //factory objects used to create event pipes
  object EventPipe {
    def apply(handler: PartialFunction[EvtBelow, Iterable[Result]]): EventPipe = new EventPipe {
      val handle = handler
    }
  }

  final def commandPipeline = cmd => commandPipe(cmd)

  final def eventPipeline = evt => eventPipe(evt)

  /**
   * changes the state of the command part of this PipePair
   * @param cmdPipe the new pipe handling commands
   */
  def becomeCmd(cmdPipe: CommandPipe) {
    commandPipe = cmdPipe
  }

  /**
   * changes the state of the event part of this PipePair
   * @param evtPipe the new pipe handling events
   */
  def becomeEvt(evtPipe: EventPipe) {
    eventPipe = evtPipe
  }

}
