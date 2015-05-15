package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.RateLimitEvent.{PercentageReached, LimitReached}
import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitStatus}
import com.soundcloud.ratelimiting.events
import com.soundcloud.ratelimiting.types
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Time, Duration}
import org.joda.time.{Period, DateTime}
import com.soundcloud.publicApiStrangler.support.TimeConversions._

class EventMapperSpec extends UnitSpecification {

  "EventMapper" should {

    "map a strangler LimitReached event to the corresponding ratelimitinglib event" in {
      val resetTime = Time.now
      val occurredAt = DateTime.now()
      val testApp = Urn("soundcloud", "applications", "test-app")
      val limitReached = LimitReached(
        RateLimitStatus.Reached(200, Duration.fromSeconds(40), Some(resetTime)),
        ApiClient(testApp))

      val result = EventMapper(limitReached, occurredAt)
      result ==== events.Event(
        occurredAt,
        Urn("soundcloud", "systems", "public-api-strangler"),
        events.LimitReached(types.ApiClient(testApp), types.RateLimit.General(Period.seconds(40), 200), Some(resetTime.toJodaDateTime))
      )
    }

    "map a strangler PercentageReached event to the corresponding ratelimitinglib CheckpointReached event" in {
      val resetTime = Time.now
      val occurredAt = DateTime.now()
      val testApp = Urn("soundcloud", "applications", "test-app")
      val limitReached = PercentageReached(
        RateLimitStatus.Advancing(100, 200, Duration.fromSeconds(40), Some(resetTime)),
        ApiClient(testApp))

      val result = EventMapper(limitReached, occurredAt)
      result ==== events.Event(
        occurredAt,
        Urn("soundcloud", "systems", "public-api-strangler"),
        events.CheckpointReached(
          types.ApiClient(testApp),
          types.RateLimit.General(Period.seconds(40), 200),
          Some(resetTime.toJodaDateTime),
          100
        )
      )
    }


  }

}
