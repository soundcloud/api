package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util.Time

sealed trait RateLimitStatus

object RateLimitStatus {
  case class Reached(resetTime: Option[Time]) extends RateLimitStatus
  case class Advancing(remainingRequests: Long, resetTime: Option[Time]) extends RateLimitStatus
}
