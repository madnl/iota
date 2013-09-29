package com.adobe.iota.actor.pipeline

import akka.util.ByteString
import scala.annotation.tailrec

/**
 *
 */
object ByteStringUtils {

  implicit class ByteStringHelper(val bs: ByteString) extends AnyVal {

    def splitInChunks(count: Int): Seq[ByteString] = {
      val chunkSize = if (bs.size%count == 0) bs.size/count else (bs.size/count)+1
      splitInChunksOfSize(chunkSize)
    }

    def splitInChunksOfSize(chunkSize: Int): Seq[ByteString] = {

      @tailrec
      def splitRec(bs: ByteString, accum: List[ByteString]): List[ByteString] = {
        if (bs.size <= chunkSize)
          (bs :: accum).reverse
        else {
          val (first, rest) = bs.splitAt(chunkSize)
          splitRec(rest, first :: accum)
        }
      }

      splitRec(bs, List())
    }

  }

}
