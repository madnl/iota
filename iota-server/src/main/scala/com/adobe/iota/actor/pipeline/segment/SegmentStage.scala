package com.adobe.iota.actor.pipeline.segment

import akka.io.{HasLogging, PipelineStage}
import com.adobe.iota.actor.pipeline.{RequestSegment, ResponseSegment}
import akka.util.ByteString

/**
 * This pipeline stage breaks down the binary stream of data from the client into more structured segments.
 */
class SegmentStage extends PipelineStage[HasLogging, ResponseSegment, ByteString, RequestSegment, ByteString] {
  override def apply(ctx: HasLogging) = new SegmentEventPipe with SegmentCommandPipe{
    def context = ctx
  }
}
