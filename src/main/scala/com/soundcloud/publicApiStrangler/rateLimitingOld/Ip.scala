package com.soundcloud.publicApiStrangler.rateLimitingOld

case class Ip(address: String) {
  require(address != null, "IP address must not be null")
}
