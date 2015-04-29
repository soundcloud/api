package com.soundcloud.publicApiStrangler.rateLimiting.semanticevents

import org.joda.time.format.ISOPeriodFormat
import org.joda.time.{DateTime, Period}
import play.api.libs.json._

case class RateLimitQuotaReached(
  percentage: Int,
  limit: Int,
  period: Period,
  reset: Option[DateTime]) extends SemanticEventPayload {

  def eventType = "publicapistrangler.ratelimit:quota.reached"

}

object RateLimitQuotaReached {
  implicit val periodWrites: Writes[Period] = Writes(ISOPeriodFormat.standard().print _ andThen JsString)
  implicit val rateLimitingQuotaReachedWrites: Writes[RateLimitQuotaReached] = Json.writes[RateLimitQuotaReached]
}

