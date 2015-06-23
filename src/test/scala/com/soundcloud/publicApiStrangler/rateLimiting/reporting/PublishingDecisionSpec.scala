package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.ratelimiting.core._
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.{DateTime, DateTimeZone, Period}

class PublishingDecisionSpec extends UnitSpecification {

  "PublishingDecision" should {

    trait Context extends Scope {
      val someClient = ClientApplication(Urn("soundcloud", "applications", "test-app"))
      val resetTime = Some(DateTime.now(DateTimeZone.UTC))
      val rateLimit = RateLimit(EndpointGroup("default", ".*".r), Period.seconds(50), 5000 * 100, RateLimitMode.Probing)
      def event(requestCount: Int) = {
        Event(DateTime.now(DateTimeZone.UTC), Urn("soundcloud", "systems", "someone"), ReachedEventPayload(
          someClient, RateLimitIdentity.forRateLimit(rateLimit), resetTime, requestCount, RateLimitMode.Probing
        ))
      }
    }

    "decide not to publish if the event is a PercentageReached with a non-checkpoint percentage of the limit" in new Context {
      PublishingDecision.shouldPublish(event(17 * 5000)) must beFalse
      PublishingDecision.shouldPublish(event(5000 * 75 + 1)) must beFalse
    }

    "decide to publish if the event is a PercentageReached with a checkpoint percentage of the limit" in new Context {
      PublishingDecision.shouldPublish(event(5000 * 75)) must beTrue
      PublishingDecision.shouldPublish(event(5000 * 90)) must beTrue
      PublishingDecision.shouldPublish(event(5000 * 100)) must beTrue
    }

    "decide to publish if the event is a PercentageReached with a checkpoint request count of 15K or 65K" in new Context {
      PublishingDecision.shouldPublish(event(15000)) must beTrue
      PublishingDecision.shouldPublish(event(65000)) must beTrue
    }
  }
}
