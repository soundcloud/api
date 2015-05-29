package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent.CheckpointReached
import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitStatus}
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.{events, types}
import com.soundcloud.ratelimiting.types.{RateLimitIdentity, RateLimit}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.Time
import org.joda.time.{DateTime, Period}

class EventMapperSpec extends UnitSpecification {

  "EventMapper" should {

    trait Context extends Scope {
      val resetTime = Time.now
      val occurredAt = DateTime.now()
      val testApp = Urn("soundcloud", "applications", "test-app")
      val rateLimit = RateLimit.General(Period.seconds(40), 200)
      val rateLimitIdentity = RateLimitIdentity.forRateLimit(rateLimit)
    }

    "map a strangler CheckpointReached event that has reached limit to the corresponding ratelimitinglib event" in new Context {
      val limitReached = CheckpointReached(
        RateLimitStatus.reached(rateLimit, Some(resetTime)),
        ApiClient(testApp))

      val result = EventMapper(limitReached, occurredAt)
      result ==== events.Event(
        occurredAt,
        Urn("soundcloud", "systems", "public-api-strangler"),
        events.LimitReached(types.ApiClient(testApp), rateLimitIdentity, Some(resetTime.toJodaDateTime))
      )
    }

    "map a strangler CheckpointReached event that has reached an interesting point to the corresponding ratelimitinglib CheckpointReached event" in new Context {
      val limitReached = CheckpointReached(
        RateLimitStatus(rateLimit, 100, Some(resetTime)),
        ApiClient(testApp))

      val result = EventMapper(limitReached, occurredAt)
      result ==== events.Event(
        occurredAt,
        Urn("soundcloud", "systems", "public-api-strangler"),
        events.CheckpointReached(
          types.ApiClient(testApp),
          rateLimitIdentity,
          Some(resetTime.toJodaDateTime),
          100
        )
      )
    }
  }
}
