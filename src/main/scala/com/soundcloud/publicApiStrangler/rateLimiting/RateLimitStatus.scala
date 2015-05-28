package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.publicApiStrangler.standards.PublicApiStandards._
import com.soundcloud.ratelimiting.types.{RateLimitIdentity, RateLimit}
import com.soundcloud.publicApiStrangler.support.TimeConversions._

import com.twitter.util._
import play.api.libs.json.{Json, Writes}
import com.soundcloud.ratelimiting.events.JsonProtocol._

sealed trait RateLimitStatus {
  def rateLimit: RateLimit
  def requestCount: Long
  lazy val maxNrOfRequests: Long = rateLimit.maximumNrOfRequests
  lazy val duration: Duration = rateLimit.timeWindow.toTwitterDuration
  def resetTime: Option[Time]
  def percentageUsed: Int
}

object RateLimitStatus {

  case class Reached(rateLimit: RateLimit, resetTime: Option[Time]) extends RateLimitStatus {

    val requestCount = maxNrOfRequests
    val percentageUsed = 100

  }

  case class Advancing(rateLimit: RateLimit, requestCount: Long, resetTime: Option[Time]) extends RateLimitStatus {

    val remainingRequests = maxNrOfRequests - requestCount
    val percentageUsed = (requestCount.toDouble / maxNrOfRequests * 100).toInt

  }

  object Reached {
    def from(rateLimit: RateLimit)(resetTime: Option[Time]): Reached = {
      Reached(rateLimit, resetTime)
    }
  }

  object Advancing {
    def from(rateLimit: RateLimit)(requestCount: Long, resetTime: Option[Time]): Advancing = {
      Advancing(rateLimit, requestCount, resetTime)
    }
  }

  implicit val writes: Writes[RateLimitStatus] = Writes {
    case reached: Reached =>
      Json.obj(
        "rate_limit_status" -> "reached",
        "rate_limit" -> RateLimitIdentity.forRateLimit(reached.rateLimit),
        "reset_time" -> reached.resetTime
      )
    case advancing: Advancing =>
      Json.obj(
        "rate_limit_status" -> "advancing",
        "rate_limit" -> RateLimitIdentity.forRateLimit(advancing.rateLimit),
        "remaining_requests" -> advancing.remainingRequests,
        "reset_time" -> advancing.resetTime
      )
  }
}