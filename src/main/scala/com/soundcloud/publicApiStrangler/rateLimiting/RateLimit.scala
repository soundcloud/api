package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util.Duration

sealed trait RateLimit {
  def ttl: Duration
  def maximumNrOfRequests: Int
  def identifier: String
}

object RateLimit {
  case class General(ttl: Duration, maximumNrOfRequests: Int) extends RateLimit {
    def identifier: String = s"${maximumNrOfRequests}_requests_per_${ttl.inSeconds}_seconds"
  }
}


