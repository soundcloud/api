package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent.CheckpointReached
import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitStatus}
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.{events, types}
import com.soundcloud.ratelimiting.types.{RateLimitIdentity, RateLimit}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.Time
import org.joda.time.{DateTimeZone, DateTime, Period}

class EventMapperSpec extends UnitSpecification {

  "EventMapper" should {

    trait Context extends Scope {
      val resetTime = Time.now
      val testApp = Urn("soundcloud", "applications", "test-app")
      val rateLimit = RateLimit.General(Period.seconds(40), 200)
      val rateLimitIdentity = RateLimitIdentity.forRateLimit(rateLimit)
    }

    "map a strangler CheckpointReached event to the corresponding ratelimitinglib event" in new Context {
      val limitReached = CheckpointReached(
        RateLimitStatus.reached(rateLimit, Some(resetTime)),
        ApiClient(testApp))

      val result = EventMapper(limitReached)
      result ==== events.Event(
        limitReached.occurredAt,
        Urn("soundcloud", "systems", "public-api-strangler"),
        events.ReachedEventPayload(types.ApiClient(testApp), rateLimitIdentity, Some(resetTime.toJodaDateTime), 200)
      )
    }
  }
}
