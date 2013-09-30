package org.madnl.iota.actor.pipeline.segment

import org.madnl.iota.actor.pipeline._
import akka.util.ByteString
import akka.io.PipelineContext

/**
 * The command pipe of the segment stage
 */
trait SegmentCommandPipe
  extends StateMachine[ResponseSegment, ByteString, RequestSegment, ByteString] with HasContext[PipelineContext] {

  becomeCmd {
    CommandPipe {
      case FullHeader(data) => {
        context.singleCommand(data)
      }
      case BodyPart(data) => {
        context.singleCommand(data)
      }
    }
  }

}
