package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent.CheckpointReached
import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitStatus}
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.types.RateLimitIdentity
import com.soundcloud.ratelimiting.{events, types}
import org.joda.time.DateTime

private[reporting] object EventMapper extends ((CheckpointReached, DateTime) => events.Event[events.ReachedEventPayload]) {

  def apply(event: CheckpointReached, occurredAt: DateTime): events.Event[events.ReachedEventPayload] = {
    val payload = if (event.hasReachedLimit) {
      limitReached(event.status, event.apiClient)
    } else {
      checkpointReached(event.status, event.apiClient)
    }
    events.Event(occurredAt, ReportingRateLimitEventListener.stranglerUrn, payload)
  }

  private def limitReached(reached: RateLimitStatus, client: ApiClient): events.LimitReached = {
    events.LimitReached(
      types.ApiClient(client.urn),
      RateLimitIdentity.forRateLimit(reached.rateLimit),
      reached.resetTime.map(_.toJodaDateTime))
  }

  private def checkpointReached(reached: RateLimitStatus, client: ApiClient): events.CheckpointReached = {
    events.CheckpointReached(
      types.ApiClient(client.urn),
      RateLimitIdentity.forRateLimit(reached.rateLimit),
      reached.resetTime.map(_.toJodaDateTime),
      reached.requestCount)
  }
}
