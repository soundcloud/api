package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util.Duration
import org.joda.time.Period

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
        case Pattern(maximumNrOfRequests, ttlString) => General(parseDuration(ttlString), maximumNrOfRequests.toLong)
      }.toSet
    }

    private def parseDuration(iso8601Period: String): Duration = {
      val seconds = Period.parse(iso8601Period).toStandardSeconds.getSeconds
      Duration.fromSeconds(seconds)
    }

  }
}


