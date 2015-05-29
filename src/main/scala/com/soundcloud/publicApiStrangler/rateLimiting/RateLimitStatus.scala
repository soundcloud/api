package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.publicApiStrangler.standards.PublicApiStandards._
import com.soundcloud.ratelimiting.types.{RateLimitIdentity, RateLimit}
import com.soundcloud.publicApiStrangler.support.TimeConversions._

import com.twitter.util._
import play.api.libs.json.{Json, Writes}
import com.soundcloud.ratelimiting.events.JsonProtocol._

case class RateLimitStatus(rateLimit: RateLimit,
                           requestCount: Int,
                           resetTime: Option[Time]) {

  lazy val hasReached: Boolean = maximumNrOfRequests == requestCount
  lazy val remainingRequests: Int = maximumNrOfRequests - requestCount
  lazy val percentageUsed: Int = (requestCount.toDouble / maximumNrOfRequests * 100).toInt
  lazy val maximumNrOfRequests: Int = rateLimit.maximumNrOfRequests.toInt
  lazy val duration: Duration = rateLimit.timeWindow.toTwitterDuration
}

object RateLimitStatus {

  def reached(rateLimit: RateLimit, resetTime: Option[Time]): RateLimitStatus = {
    RateLimitStatus(rateLimit, rateLimit.maximumNrOfRequests.toInt, resetTime)
  }

  implicit val writes: Writes[RateLimitStatus] = Writes { rateLimitStatus =>
    Json.obj(
      "rate_limit" -> RateLimitIdentity.forRateLimit(rateLimitStatus.rateLimit),
      "remaining_requests" -> rateLimitStatus.remainingRequests,
      "reset_time" -> rateLimitStatus.resetTime
    )
  }
}