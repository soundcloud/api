package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util._
import play.api.libs.json.{Json, Writes}
import com.soundcloud.publicApiStrangler.standards.PublicApiStandards._

sealed trait RateLimitStatus

object RateLimitStatus {
  case class Reached(maxNrOfRequests: Long, resetTime: Option[Time]) extends RateLimitStatus
  case class Advancing(remainingRequests: Long, resetTime: Option[Time]) extends RateLimitStatus

  implicit val writes: Writes[RateLimitStatus] = Writes {
    case Reached(maxNrOfRequests, resetTime) =>
      Json.obj(
        "rate_limit_status" -> "reached",
        "max_nr_of_requests" -> maxNrOfRequests,
        "reset_time" -> resetTime
      )
    case Advancing(remainingRequests, resetTime) =>
      Json.obj(
        "rate_limit_status" -> "advancing",
        "remaining_requests" -> remainingRequests,
        "reset_time" -> resetTime
      )
  }
}