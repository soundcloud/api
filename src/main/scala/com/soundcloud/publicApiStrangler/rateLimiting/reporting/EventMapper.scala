package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitStatus, RateLimitEvent}
import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent.Reached
import com.soundcloud.ratelimiting.types.RateLimitIdentity
import com.soundcloud.ratelimiting.{types, events}
import com.soundcloud.publicApiStrangler.support.TimeConversions._

import org.joda.time.DateTime

private[reporting] object EventMapper extends ((Reached, DateTime) => events.Event[events.ReachedEventPayload]) {

  def apply(reached: Reached, occurredAt: DateTime): events.Event[events.ReachedEventPayload] = {
    val payload = reached match {
      case RateLimitEvent.LimitReached(status, client) => limitReached(status, client)
      case RateLimitEvent.PercentageReached(status, client) => checkpointReached(status, client)
    }
    events.Event(occurredAt, ReportingRateLimitEventListener.stranglerUrn, payload)
  }

  private def limitReached(reached: RateLimitStatus.Reached, client: ApiClient): events.LimitReached = {
    events.LimitReached(
      types.ApiClient(client.urn),
      RateLimitIdentity.forRateLimit(reached.rateLimit),
      reached.resetTime.map(_.toJodaDateTime))
  }

  private def checkpointReached(reached: RateLimitStatus.Advancing, client: ApiClient): events.CheckpointReached = {
    events.CheckpointReached(
      types.ApiClient(client.urn),
      RateLimitIdentity.forRateLimit(reached.rateLimit),
      reached.resetTime.map(_.toJodaDateTime),
      reached.requestCount.toInt)
  }

}
