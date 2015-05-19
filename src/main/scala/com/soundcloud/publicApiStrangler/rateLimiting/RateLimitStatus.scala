package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.ratelimiting.types.RateLimit
import com.twitter.util._
import play.api.libs.json.{Json, Writes}
import com.soundcloud.publicApiStrangler.standards.PublicApiStandards._
import com.soundcloud.publicApiStrangler.support.TimeConversions._

sealed trait RateLimitStatus {
  def requestCount: Long
  def maxNrOfRequests: Long
  def duration: Duration
  def resetTime: Option[Time]
  def percentageUsed: Int
}

object RateLimitStatus {

  case class Reached(
                      maxNrOfRequests: Long,
                      duration: Duration,
                      resetTime: Option[Time]) extends RateLimitStatus {

    val requestCount = maxNrOfRequests
    val percentageUsed = 100

  }

  case class Advancing(
                        requestCount: Long,
                        maxNrOfRequests: Long,
                        duration: Duration,
                        resetTime: Option[Time]) extends RateLimitStatus {

    val remainingRequests = maxNrOfRequests - requestCount
    val percentageUsed = (requestCount / maxNrOfRequests).toInt

  }

  object Reached {
    def from(rateLimit: RateLimit)(resetTime: Option[Time]): Reached = {
      Reached(rateLimit.maximumNrOfRequests, rateLimit.timeWindow.toTwitterDuration, resetTime)
    }
  }

  object Advancing {
    def from(rateLimit: RateLimit)(requestCount: Long, resetTime: Option[Time]): Advancing = {
      Advancing(requestCount, rateLimit.maximumNrOfRequests, rateLimit.timeWindow.toTwitterDuration, resetTime)
    }
  }

  implicit val writes: Writes[RateLimitStatus] = Writes {
    case reached: Reached =>
      Json.obj(
        "rate_limit_status" -> "reached",
        "max_nr_of_requests" -> reached.maxNrOfRequests,
        "reset_time" -> reached.resetTime
      )
    case advancing: Advancing =>
      Json.obj(
        "rate_limit_status" -> "advancing",
        "remaining_requests" -> advancing.remainingRequests,
        "reset_time" -> advancing.resetTime
      )
  }
}