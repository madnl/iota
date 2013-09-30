package org.madnl.iota.http

/**
 * Header which can be found in requests or responses
 * @param name The name of the header
 * @param value The string value of the header
 */
case class Header(name: String, value: String)
