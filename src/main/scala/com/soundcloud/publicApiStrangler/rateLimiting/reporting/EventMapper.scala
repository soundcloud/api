package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent.CheckpointReached
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.core._
import com.soundcloud.ratelimiting.events._

private[reporting] object EventMapper extends (CheckpointReached => Event[ReachedEventPayload]) {

  def apply(event: CheckpointReached): Event[ReachedEventPayload] = {
    Event(
      event.occurredAt,
      ReportingRateLimitEventListener.stranglerUrn,
      ReachedEventPayload(
        event.clientApplication,
        RateLimitIdentity.forRateLimit(event.status.rateLimit),
        event.status.resetTime.map(_.toJodaDateTime),
        event.status.requestCount,
        event.status.rateLimit.mode
      )
    )
  }
}
