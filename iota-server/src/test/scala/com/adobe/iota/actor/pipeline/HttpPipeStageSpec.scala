package com.adobe.iota.actor.pipeline

import org.scalatest.{BeforeAndAfter, FunSpec}
import akka.io._
import akka.event.LoggingAdapter
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import akka.util.{ByteString, ByteStringBuilder}
import scala.Some
import com.adobe.iota.api.Request
import scala.util.{Failure, Success}
import ByteStringUtils._
import scala.collection.mutable.ListBuffer
import org.scalacheck.Gen
import com.adobe.iota.http.Methods
import akka.actor.ActorContext
import com.google.common.net.HttpHeaders

/**
 *
 */
class HttpPipeStageSpec extends FunSpec with BeforeAndAfter with MockitoSugar with ShouldMatchers {

  private val log = new LoggingAdapterStub
  log.enabled = false

  describe("A HTTP pipeline stage in single-request mode") {

    it("should handle body-less requests sent as one chunk") {
      runScenario(bodyLessRequests(1), chunkCount = 1)
    }

    it("should handle body-less requests sent as multiple chunks") {
      runScenario(bodyLessRequests(1), 2)
      runScenario(bodyLessRequests(1), 3)
      runScenario(bodyLessRequests(1), 15)
    }

    it("should handle requests with body sent in one chunk") {
      runScenario(bodyRequests(1, maxSize = 2000), chunkCount = 1)
    }

    it("should handle requests with body sent in multiple chunks") {
      runScenario(bodyRequests(1, maxSize = 2000), chunkCount = 2)
      runScenario(bodyRequests(1, maxSize = 2000), chunkCount = 3)
      runScenario(bodyRequests(1, maxSize = 2000), chunkCount = 10)
      runScenario(bodyRequests(1, maxSize = 2000), chunkCount = 30)
    }
  }

  describe("A HTTP pipeline stage in pipelined-request mode") {
    it("should handle body-less requests sent as one chunk") {
      runScenario(bodyLessRequests(100), chunkCount = 1)
    }

    it("should handle body-less requests sent as many chunks") {
      val requests = bodyLessRequests(100)
      runScenario(requests, chunkCount = 37)
      runScenario(requests, chunkCount = 2)
      runScenario(requests, chunkCount = 10)
      runScenario(requests, chunkCount = 102)
    }

    it("should handle requests with body sent as one chunk") {
      runScenario(bodyRequests(100, maxSize = 2500), chunkCount = 1)
    }

    it("should handle requests with body sent as many chunks") {
      val requests = bodyRequests(100, maxSize = 2500)
      runScenario(requests, chunkCount = 2)
      runScenario(requests, chunkCount = 20)
      runScenario(requests, chunkCount = 37)
      runScenario(requests, chunkCount = 103)
    }
  }

  private def runScenario(providedRequests: Seq[TestRequest], chunkCount: Int) {
    val allData = providedRequests.map(_.toByteString).foldLeft(ByteString())(_ ++ _)
    val chunks = allData.splitInChunks(chunkCount)
    assert(chunks.size == chunkCount)
    assert(chunks.foldLeft(ByteString())(_ ++ _) == allData)
    val receivedRequests = ListBuffer[TestRequest]()
    var requestOpt = None: Option[Request]
    val bodyBuilder = new ByteStringBuilder
    val pipe = PipelineFactory.buildWithSinkFunctions(mkContext(log), HttpPipeline(mock[ConnectionControl]))(
      commandSink = {
        case x => println("Command: " + x)
      },
      eventSink = {
        case Success(RequestPreamble(r, _)) => {
          requestOpt should not be 'defined
          requestOpt = Some(r)
        }
        case Success(BodyPart(data)) => {
          requestOpt should be('defined)
          bodyBuilder.append(data)
        }
        case Success(EndOfRequest) => {
          requestOpt should be('defined)
          val testReq = TestRequest(requestOpt.get, bodyBuilder.result())
          receivedRequests.append(testReq)
          //resetting
          requestOpt = None
          bodyBuilder.clear()
        }
        case Failure(cause) => {
          throw cause
        }
      }
    )
    chunks foreach pipe.injectEvent
    receivedRequests.toList should equal(providedRequests)
  }

  private def bodyLessRequests(n: Int) = mkRequests(n, Seq(Methods.Get, Methods.Head), 0)

  private def bodyRequests(n: Int, maxSize: Int = 2000) = mkRequests(n, Seq(Methods.Post, Methods.Put), maxSize)

  private def mkRequests(n: Int, methods: Seq[String], maxBodySize: Int) =
    Gen.listOfN(n, genRequest(methods, maxBodySize)).sample.get

  private def genRequest(methods: Seq[String], maxBodySize: Int): Gen[TestRequest] = {
    for {
      method <- Gen.oneOf(methods)
      pathSuffix <- alphaString(0, 300)
      path = "/" + pathSuffix
      bodyBytes <- listBetween(0, maxBodySize, Gen.choose(-128, 127).map(_.toByte))
      body = ByteString(bodyBytes: _*)
      hs <- headersGen(method, body)
    } yield
      TestRequest(method, path, hs, body)
  }

  private def headersGen(method: String, body: ByteString) = {
    for {
      hs1 <- listBetween(0, 2, headerGen)
      hs2 <- listBetween(0, 58, headerGen)
    } yield {
      if (Methods.hasNoBody(method)) {
        hs1 ++ hs2
      } else {
        hs1 ++ List(HttpHeaders.CONTENT_LENGTH -> body.size.toString) ++ hs2
      }
    }
  }

  private def alphaString(minSize: Int, maxSize: Int) = {
    for(chars <- listBetween(minSize, maxSize, Gen.alphaChar))
    yield new String(chars.toArray)
  }

  private def headerGen = {
    for {
      name <- alphaString(5, 20)
      value <- alphaString(5, 20)
    } yield (name, value)
  }


  private def listBetween[T](minSize: Int, maxSize: Int, gen: Gen[T]): Gen[List[T]] = {
    for {
      size <- Gen.choose(minSize, maxSize)
      items <- Gen.listOfN(size, gen)
    } yield items
  }

  private def mkContext(log: LoggingAdapter) = new HasActorContext with HasLogging {
    def getContext = mock[ActorContext]

    def getLogger = log
  }
}
