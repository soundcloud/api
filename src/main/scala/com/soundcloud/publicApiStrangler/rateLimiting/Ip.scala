package com.soundcloud.publicApiStrangler.rateLimiting

case class Ip(address: String) {
  require(address != null, "IP address must not be null")
}
