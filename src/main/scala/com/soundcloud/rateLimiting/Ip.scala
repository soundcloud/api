package com.soundcloud.rateLimiting

case class Ip(address: String) {
  require(address != null, "IP address must not be null")
}
