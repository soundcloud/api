package com.soundcloud.publicApiStrangler.rateLimiting.semanticevents

import org.joda.time.format.ISOPeriodFormat
import org.joda.time.{DateTime, Period}
import play.api.libs.json._

case class RateLimitingQuotaReached(percentage: Int,
                                    limit: Int,
                                    period: Period,
                                    reset: Option[DateTime])

object RateLimitingQuotaReached {
  implicit val periodWrites: Writes[Period] = Writes(ISOPeriodFormat.standard().print _ andThen JsString)
  implicit val rateLimitingQuotaReachedWrites: Writes[RateLimitingQuotaReached] = Json.writes[RateLimitingQuotaReached]
}

