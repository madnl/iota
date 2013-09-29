package com.adobe.iota.actor.pipeline.segment

import com.adobe.iota.actor.pipeline.ProtocolClientException

/**
 * Trait that implements the state-keeping for the body transfer method of the current request
 */
trait BodyTransferSemantics {

  private var nextBodyTransfer = None: Option[BodyTransferMethod]

  protected def establishBodyTransfer(transferMethod: BodyTransferMethod) {
    if (nextBodyTransfer.isDefined)
      throw new ProtocolClientException("Body transfer method was already established")
    nextBodyTransfer = Some(transferMethod)
  }

  protected def resetBodyTransfer() {
    nextBodyTransfer = None
  }

  protected def bodyTransfer: BodyTransferMethod = nextBodyTransfer.getOrElse(NoBody)

}
