package com.soundcloud.publicApiStrangler.rateLimiting.reporting

import com.soundcloud.publicApiStrangler.rateLimiting.{ApiClient, RateLimitEvent, RateLimitStatus}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Duration, Time}

class PublishingDecisionSpec extends UnitSpecification {

  "PublishingDecision" should {

    trait Context extends Scope {
      val someClient = ApiClient(Urn("soundcloud", "applications", "test-app"))
      val resetTime = Some(Time.now)
      def event(percentage: Int) = {
        RateLimitEvent.PercentageReached(RateLimitStatus.Advancing(
          percentage, 100, Duration.fromSeconds(50), resetTime), someClient)
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
      val events = event(75) :: event(90) :: Nil
      (events map PublishingDecision.shouldPublish) ==== (events map PublishingDecision.DoPublish.apply)
    }

    "decide to publish if the event is a LimitReached event" in new Context {
      val event = RateLimitEvent.LimitReached(
        RateLimitStatus.Reached(100, Duration.fromSeconds(50), resetTime), someClient)
      PublishingDecision.shouldPublish(event) ==== PublishingDecision.DoPublish(event)
    }

  }

}
