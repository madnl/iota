package com.adobe.iota.actor.pipeline.segment

/**
 * Methods to transfer the body of a request
 */
private[pipeline] sealed trait BodyTransferMethod

private[pipeline] case object NoBody extends BodyTransferMethod

private[pipeline] case class IdentityTransfer(contentLength: Int) extends BodyTransferMethod

private[pipeline] case object ChunkedTransfer extends BodyTransferMethod
