package com.adobe.iota.actor.pipeline

import akka.io.PipelineContext

/**
 * To be mixed by objects requiring access to a pipeline context
 */
trait HasContext[T <: PipelineContext] {

  /**
   * Provide the context for this pipeline
   * @return the pipeline context
   */
  def context: T

}
