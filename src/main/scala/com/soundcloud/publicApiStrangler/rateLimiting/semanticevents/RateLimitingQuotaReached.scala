package com.soundcloud.publicApiStrangler.rateLimiting.semanticevents

import org.joda.time.format.ISOPeriodFormat
import org.joda.time.{DateTime, Period}
import play.api.libs.json._

trait SemanticEvent {
  def at: DateTime
  def context: Context
}

case class Context(app: String)

case class RateLimitingQuotaReached(
  at: DateTime,
  context: Context,
  reached: RateLimitingQuotaReached.Payload
) extends SemanticEvent

object RateLimitingQuotaReached {

  case class Payload(
    quota: Int,
    client: String,
    limit: Int,
    period: Period,
    reset: DateTime
  )

  private implicit val periodWrites: Writes[Period] = Writes(ISOPeriodFormat.standard().print _ andThen JsString)

  private implicit val contextWrites: Writes[Context] = Json.writes[Context]

  private implicit val payloadWrites: Writes[Payload] = Json.writes[Payload]

  implicit val rateLimitingQuotaReachedWrites: Writes[RateLimitingQuotaReached] = Json.writes[RateLimitingQuotaReached]

}
