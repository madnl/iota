package com.adobe.iota.api

import java.net.URI
import scala.util.matching.Regex
import java.util.regex.Pattern

/**
 * Identifies a set of resources by matching the URI of the resource
 */
trait ResourcePattern {

  /**
   * Specifies which resources are in this pattern by checking if the provided
   * URI is matched by the pattern
   * @param uri The URI to be checked
   * @return True if the URI matches, false otherwise
   */
  def matchURI(uri: URI): Boolean
}

/**
 * Pattern for resources that start with a given prefix
 * @param prefix The prefix prefix
 */
class WithPathPrefix(prefix: String) extends ResourcePattern {
  def matchURI(uri: URI) = uri.getPath.startsWith(prefix)
}

/**
 * Pattern that matches anything
 */
object Anything extends ResourcePattern {
  def matchURI(uri: URI) = true
}

/**
 * Pattern matching provided by regular expressions
 * @param regex The regex which will match the URIs
 */
class PathRegex(regex: String) extends ResourcePattern {

  private val pattern = Pattern.compile(regex)

  def matchURI(uri: URI) = pattern.matcher(uri.getPath).matches()

}

/**
 * Exactly matches a certain path
 * @param path The path to be matched
 */
class ExactMatch(path: String) extends ResourcePattern {
  def matchURI(uri: URI) = uri.getPath == path
}