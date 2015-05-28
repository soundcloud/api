package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitEvent, RateLimitStatus}
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.Time
import org.joda.time.Period

class PublishingDecisionSpec extends UnitSpecification {

  "PublishingDecision" should {

    trait Context extends Scope {
      val someClient = ApiClient(Urn("soundcloud", "applications", "test-app"))
      val resetTime = Some(Time.now)
      val rateLimit = RateLimit.General(Period.seconds(50), 100)
      def event(percentage: Int) = {
        RateLimitEvent.PercentageReached(RateLimitStatus.Advancing(
          rateLimit, percentage, resetTime), someClient)
      }
    }

    "decide not to publish if the event is an Overflowing event" in new Context {
      val event = RateLimitEvent.Overflowing(someClient)
      PublishingDecision.shouldPublish(event) ==== PublishingDecision.DoNotPublish
    }

    "decide not to publish if the event is a PercentageReached with a non-checkpoint percentage of the limit" in new Context {
      PublishingDecision.shouldPublish(event(17)) ==== PublishingDecision.DoNotPublish
    }

    "decide to publish if the event is a PercentageReached with a checkpoint percentage of the limit" in new Context {
      val events = event(75) :: event(90) :: event(100) :: Nil
      (events map PublishingDecision.shouldPublish) ==== (events map PublishingDecision.DoPublish.apply)
    }

    "decide to publish if the event is a PercentageReached with a checkpoint request count of 15K or 65K" in new Context {
      val events = event(15000) :: event(65000) :: Nil
      (events map PublishingDecision.shouldPublish) ==== (events map PublishingDecision.DoPublish.apply)
    }

    "decide to publish if the event is a LimitReached event" in new Context {
      val event = RateLimitEvent.LimitReached(
        RateLimitStatus.Reached(rateLimit, resetTime), someClient)
      PublishingDecision.shouldPublish(event) ==== PublishingDecision.DoPublish(event)
    }

  }

}
