package org.madnl.iota.http

/**
 * Data structure that acts as a container for the headers of a requests or response
 * @param headers The contained list of headers
 */
class HeaderMap private(val headers: List[Header]) extends Iterable[Header] {

  private val mapping = (for (h <- headers) yield (h.name.toLowerCase, h)).toMap

  /**
   * Get the header value for the specified header name
   * @param name The name of the header
   * @return The string value of the header
   * @throws NoSuchElementException If there is no header with the specified name
   */
  def apply(name: String): String = mapping(name.toLowerCase).value

  /**
   * Get the specified header value
   * @param name the name of the header
   * @return an optional string value
   */
  def get(name: String): Option[String] = getHeader(name).map(_.value)

  /**
   * Get the header object for the specified name
   * @param name the name of the header
   * @return an optional header object
   */
  def getHeader(name: String): Option[Header] = mapping.get(name.toLowerCase)

  /**
   * The iterator for this container
   * @return an iterator of headers
   */
  def iterator: Iterator[Header] = headers.iterator

  /**
   * Return the headers in this object as a list of pairs
   * @return a (name-value) list of pairs
   */
  def asPairs: Seq[(String, String)] = headers.map(h => (h.name, h.value))

  /**
   * The number of headers in this container
   * @return A positive number equal to the header list size
   */
  def length = headers.length

  /**
   * Get the header at the specified index
   * @param idx The index of the header
   * @return a header object
   * @throws IndexOutOfBoundsException if the index is negative or larger than the length of the container
   */
  def apply(idx: Int) = headers(idx)

  /**
   * Checks whether a certain header exists
   * @param name The name of the header
   * @return true if the header is contained in this object, false otherwise
   */
  def contains(name: String): Boolean = mapping.contains(name.toLowerCase)

  /**
   * Add a header to this container
   * @param header The header to be added
   * @return A new instance containing the provided header
   * @throws IllegalArgumentException If there is already a header with the given name
   */
  def +(header: Header): HeaderMap = prepend(header)

  /**
   * Add a header to this container
   * @param h A name-value pair representing a header
   * @return A new instance containing the provided header
   * @throws IllegalArgumentException If there is already a header with the given name
   */
  def +(h: (String, String)): HeaderMap = prepend(h._1, h._2)

  /**
   * Prepend a header with the given name and value
   * @param name The name of the header
   * @param value The value of the header
   * @return A new instance containing the specified header
   * @throws IllegalArgumentException If there is already a header with the given name
   */
  def prepend(name: String, value: String): HeaderMap = prepend(Header(name, value))

  /**
   * Prepend the specified header to the headers in this container
   * @param header The header to be added
   * @return A new header instance with the specified header
   * @throws IllegalArgumentException If there is already a header with the given name
   */
  def prepend(header: Header): HeaderMap = {
    require(!contains(header.name), s"A header with name ${header.name} was already added")
    new HeaderMap(header :: headers)
  }

  /**
   * Adds the specified name-value arguments as a header in this container. If there is already a header with
   * the given name, replace its value with the provided one
   * @param name The name of the header
   * @param value The value of the header
   * @return A new header instance with the specified header
   */
  def withHeader(name: String, value: String): HeaderMap = {
    if (contains(name)) {
      val newHeaders = for (h <- headers) yield { if (h.name == name) Header(name, value) else h}
      new HeaderMap(newHeaders)
    } else {
      prepend(name, value)
    }
  }

  /**
   * Adds a header with the given name and value if a header with the specified name doesn't exist already
   * @param name The name of the header
   * @param value The value of the header
   * @return A new header instance with the specified header if the header doesn't already exist, or this instance if it does
   */
  def addIfMissing(name: String, value: String): HeaderMap = {
    if (contains(name))
      this
    else
      prepend(name, value)
  }
}

object HeaderMap {

  /**
   * Create a header map from the given pairs
   * @param xs a list of name-value pairs
   * @return a header map with the specified pairs
   */
  def apply(xs: (String, String)*): HeaderMap = apply(xs.map({case (n, v) => Header(n, v)}).toList)

  def apply(xs: Iterable[Header]): HeaderMap = {
    val list = xs.toList
    require(list.map(_.name).toSet.size == list.size, "Duplicates are not allowed")
    new HeaderMap(list)
  }

}