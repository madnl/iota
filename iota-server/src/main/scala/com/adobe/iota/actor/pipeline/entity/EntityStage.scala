package com.adobe.iota.actor.pipeline.entity

import akka.io.{HasActorContext, HasLogging, PipelineStage}
import com.adobe.iota.actor.pipeline._


/**
 * Pipeline stage that builds request and response data structure usable for the application part
 */
private[pipeline] class EntityStage
  extends PipelineStage[HasLogging with HasActorContext, ResponsePart, ResponseSegment, RequestPart, RequestSegment] {

  override def apply(ctx: HasLogging with HasActorContext) =
    new EntityEventPipe with EntityCommandPipe with HasContext[HasActorContext] {
      def context = ctx
    }
}
