package org.madnl.iota.actor.pipeline

import akka.io.{HasActorContext, HasLogging, PipelineStage}
import org.madnl.iota.actor.pipeline.entity.EntityStage
import org.madnl.iota.actor.pipeline.segment.SegmentStage
import org.madnl.iota.actor.pipeline.connection.ConnectionStage
import akka.util.ByteString

/**
 * Factory object that provides pipeline objects for the HTTP protocol
 */
private[actor] object HttpPipeline {

  /**
   * Creates a pipeline that can handle the HTTP protocol
   * @param connectionControl A control object for the connection with the HTTP client
   * @return the pipeline object
   */
  def apply(connectionControl: ConnectionControl): PipelineStage[HasLogging with HasActorContext, ResponsePart, ByteString, RequestPart, ByteString] =
    new ConnectionStage(connectionControl) >> new EntityStage >> new SegmentStage

}
