package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitEvent, RateLimitStatus}
import com.soundcloud.ratelimiting.core.RateLimit
import com.soundcloud.ratelimiting.core.RateLimit.EndpointGroupSpecific.EndpointGroup
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.Time
import org.joda.time.Period

class PublishingDecisionSpec extends UnitSpecification {

  "PublishingDecision" should {

    trait Context extends Scope {
      val someClient = ApiClient(Urn("soundcloud", "applications", "test-app"))
      val resetTime = Some(Time.now)
      val rateLimit = RateLimit.EndpointGroupSpecific(EndpointGroup("default", ".*".r), Period.seconds(50), 5000 * 100)
      def event(requestCount: Int) = {
        RateLimitEvent.CheckpointReached(RateLimitStatus(
          rateLimit, requestCount, resetTime), someClient)
      }
    }

    "decide not to publish if the event is an Overflowing event" in new Context {
      val event = RateLimitEvent.Overflowing(someClient)
      PublishingDecision.shouldPublish(event) ==== PublishingDecision.DoNotPublish
    }

    "decide not to publish if the event is a PercentageReached with a non-checkpoint percentage of the limit" in new Context {
      PublishingDecision.shouldPublish(event(17 * 5000)) ==== PublishingDecision.DoNotPublish
      PublishingDecision.shouldPublish(event(5000 * 75 + 1)) ==== PublishingDecision.DoNotPublish
    }

    "decide to publish if the event is a PercentageReached with a checkpoint percentage of the limit" in new Context {
      val events = event(5000 * 75) :: event(5000 * 90) :: event(5000 * 100) :: Nil
      (events map PublishingDecision.shouldPublish) ==== (events map PublishingDecision.DoPublish.apply)
    }

    "decide to publish if the event is a PercentageReached with a checkpoint request count of 15K or 65K" in new Context {
      val events = event(15000) :: event(65000) :: Nil
      (events map PublishingDecision.shouldPublish) ==== (events map PublishingDecision.DoPublish.apply)
    }

    "decide to publish if the event is a LimitReached event" in new Context {
      val event = RateLimitEvent.CheckpointReached(
        RateLimitStatus.reached(rateLimit, resetTime), someClient)
      PublishingDecision.shouldPublish(event) ==== PublishingDecision.DoPublish(event)
    }
  }
}
