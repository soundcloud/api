package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.publicApiStrangler.standards.PublicApiStandards._
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.core.RateLimitIdentity
import com.soundcloud.ratelimiting.events.RateLimitEventsJsonProtocol._
import com.twitter.util._
import play.api.libs.json.{JsPath, JsObject, Json, Writes}

case class RateLimitStatus(rateLimit: RateLimitIdentity,
                           requestCount: Int,
                           resetTime: Option[Time]) {

  lazy val hasReachedLimit: Boolean = maximumNrOfRequests == requestCount
  lazy val remainingRequests: Int = maximumNrOfRequests - requestCount
  lazy val percentageUsed: Int = (requestCount.toDouble / maximumNrOfRequests * 100).toInt
  lazy val maximumNrOfRequests: Int = rateLimit.maximumNrOfRequests
  lazy val duration: Duration = rateLimit.timeWindow.toTwitterDuration
}

object RateLimitStatus {

  def reached(rateLimit: RateLimitIdentity, resetTime: Option[Time]): RateLimitStatus = {
    RateLimitStatus(rateLimit, rateLimit.maximumNrOfRequests, resetTime)
  }

  implicit val writes: Writes[RateLimitStatus] = Writes { rateLimitStatus =>
    val removeIrrelevantFields = (JsPath \ "bucket").json.prune andThen (JsPath \ "mode").json.prune
    Json.obj(
      "rate_limit" -> Json.toJson(rateLimitStatus.rateLimit).transform(removeIrrelevantFields).get,
      "remaining_requests" -> rateLimitStatus.remainingRequests,
      "reset_time" -> rateLimitStatus.resetTime
    )
  }
}