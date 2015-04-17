package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util.Duration

sealed trait RateLimit {
  def ttl: Duration
  def maximumNrOfRequests: Long
  def identifier: String
}

object RateLimit {

  case class General(ttl: Duration, maximumNrOfRequests: Long) extends RateLimit {
    def identifier: String = s"${maximumNrOfRequests}_requests_per_${ttl.inSeconds}_seconds"
  }

  object General {
    val Pattern = "([0-9]+) per (.+)".r

    def parse(s: String): Set[RateLimit] = {
      s.split(",\\s*").map {
        case Pattern(maximumNrOfRequests, ttlString) => General(Duration.parse(ttlString), maximumNrOfRequests.toLong)
      }.toSet
    }
  }
}


