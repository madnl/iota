package com.adobe.iota.http

/**
 * Utility constants referring to HTTP methods
 */
object Methods {

  val Get = "GET"
  val Head = "HEAD"
  val Post = "POST"
  val Patch = "PATCH"
  val Put = "PUT"
  val Delete = "DELETE"
  val Options = "OPTIONS"
  val Trace = "TRACE"
  val Connect = "CONNECT"

  def hasNoBody(method: String) = noBodyMethods.contains(method.toUpperCase)

  private val noBodyMethods = Set(Get, Head, Options)

  def isGet(method: String) = method equalsIgnoreCase Get
  def isHead(method: String) = method equalsIgnoreCase Head
  def isPost(method: String) = method equalsIgnoreCase Post
  def isDelete(method: String) = method equalsIgnoreCase Delete
  def isPut(method: String) = method equalsIgnoreCase Put

}
