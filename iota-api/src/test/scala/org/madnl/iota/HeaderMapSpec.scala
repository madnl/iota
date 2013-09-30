package org.madnl.iota

import org.scalatest.FunSpec
import org.madnl.iota.http.{Header, HeaderMap}
import org.scalatest.matchers.ShouldMatchers

/**
 *
 */
class HeaderMapSpec extends FunSpec with ShouldMatchers {

  describe("A header map") {
    it("should throw an exception when pre-pending duplicates") {
      val hm = HeaderMap("header1" -> "value1", "header2" -> "value2")
      evaluating { hm.prepend("header1", "value3") } should produce [IllegalArgumentException]
      evaluating { hm.prepend("header2", "value5") } should produce [IllegalArgumentException]
    }

    it("should allow pre-pending new headers") {
      val initialHeaders = List("h1" -> "v1", "h2" -> "v2")
      val hm = HeaderMap(initialHeaders: _*)
      val newHeader = "h3" -> "v3"
      val allPairs = newHeader :: initialHeaders
      val hm2 = hm + newHeader
      hm2.asPairs should equal (allPairs)
      val headers = for ((n, v) <- allPairs) yield Header(n, v)
      hm2.headers should equal(headers)
    }

    it("should prevent creation with duplicate headers") {
      evaluating {
        HeaderMap("h1" -> "v1", "h2" -> "v2", "h1" -> "v3")
      } should produce [IllegalArgumentException]
      evaluating {
        HeaderMap(Seq(Header("h1", "v1"), Header("h2", "v2"), Header("h2", "v3")))
      } should produce [IllegalArgumentException]
    }

    it("should respect the set-get contract") {
      val hm = HeaderMap("h1" -> "v1")
      val hm2 = hm + ("h2" -> "v2")
      hm2("h2") should be("v2")
      hm2("h1") should be("v1")
    }

    it("should throw errors for missing headers") {
      val hm = HeaderMap("h1" -> "v1")
      evaluating { hm("h2") } should produce [NoSuchElementException]
    }

    it("should return the valid optional value") {
      val hm = HeaderMap("h1" -> "v1", "h2" -> "v2")
      hm.getHeader("h1") should be(Some(Header("h1", "v1")))
      hm.get("h2") should be(Some("v2"))
    }

    it("should allow replacing existing headers") {
      val hm = HeaderMap("h1" -> "v1", "h2" -> "v2", "h3" -> "v3")
      val hm2 = hm.withHeader("h2", "v4")
      hm2("h2") should be ("v4")
    }

    it("should allow specifying defaults") {
      val hm = HeaderMap("h1" -> "v1", "h2" -> "v2")
      val hm2 = hm.addIfMissing("h3", "v3")
      hm2("h3") should be ("v3")
      val hm3 = hm.addIfMissing("h2", "v4")
      hm3("h2") should be ("v2")
    }
  }

}
