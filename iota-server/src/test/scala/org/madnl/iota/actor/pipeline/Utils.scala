package org.madnl.iota.actor.pipeline

import org.scalacheck.Gen

/**
 *
 */
object Utils {

  def genRequest(method: String, path: String, headerCount: Int = 10, bodySize: Int = 0) = request(method, path, headerCount, bodySize).sample.get

  def request(method: Gen[String], path: Gen[String], headerCount: Int, bodySize: Int) =
    for {
      m <- method
      p <- path
      headers <- headers(headerCount)
      body <- alphaStringOfSize(bodySize)
    } yield TestRequest(m, p, headers, body)

  def headers(count: Int) = {
    Gen.listOfN(count, header)
  }

  def header =
    for {
      name <- headerValue
      value <- headerValue
    } yield (name, value)

  def headerValue = alphaStringOfSize(10)

  def alphaStringOfSize(size: Int) = Gen.listOfN(size, Gen.alphaChar).map(chars => new String(chars.toArray))

  def stringOfSize(size: Int) = alphaStringOfSize(size).sample.get

}
