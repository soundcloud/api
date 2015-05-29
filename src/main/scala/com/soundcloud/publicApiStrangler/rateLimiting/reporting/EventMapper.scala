package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent.CheckpointReached
import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitStatus}
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.ratelimiting.types.RateLimitIdentity
import com.soundcloud.ratelimiting.{events, types}
import org.joda.time.DateTime

private[reporting] object EventMapper extends (CheckpointReached => events.Event[events.ReachedEventPayload]) {

  def apply(event: CheckpointReached): events.Event[events.ReachedEventPayload] = {
    Event(
      event.occurredAt,
      ReportingRateLimitEventListener.stranglerUrn,
      ReachedEventPayload(
        types.ApiClient(event.apiClient.urn),
        RateLimitIdentity.forRateLimit(event.status.rateLimit),
        event.status.resetTime.map(_.toJodaDateTime),
        event.status.requestCount
      )
    )
  }
}
