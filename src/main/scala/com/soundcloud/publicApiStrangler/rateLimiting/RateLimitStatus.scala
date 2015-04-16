package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util._
import play.api.libs.json.{JsString, Json, Writes}

sealed trait RateLimitStatus

object RateLimitStatus {
  case class Reached(maxNrOfRequests: Long, resetTime: Option[Time]) extends RateLimitStatus
  case class Advancing(remainingRequests: Long, resetTime: Option[Time]) extends RateLimitStatus

  // TODO: MOVE TO JVMKIT
  private val timeFormat = new TimeFormat("yyyy/MM/dd hh:mm:ss ZZZZ")

  // TODO: MOVE TO JVMKIT
  private implicit val timeWrites: Writes[Time] = Writes(timeFormat.format _ andThen JsString)

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