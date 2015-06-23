package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.ratelimiting.core.RateLimitConfiguration.Bucket
import com.soundcloud.ratelimiting.core._
import com.soundcloud.scalakit.Urn
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, Await, Time}
import org.jboss.netty.handler.codec.http.DefaultHttpHeaders
import org.joda.time.Period

class RateLimiterSpec extends UnitSpecification {

  "RateLimiter" should {

    trait Context extends Scope {
      val endpointGroup = EndpointGroup("default", ".*".r)

      val config1 = RateLimitConfiguration(Bucket.Default, Period.hours(24), 15000)
      val config2 = RateLimitConfiguration(Bucket.Default, Period.hours(1), 150)
      val config3 = RateLimitConfiguration(Bucket.Default, Period.hours(24), 20000)
      val config4 = RateLimitConfiguration(Bucket.Default, Period.hours(24), 20000)

      val rateLimit1 = RateLimit(endpointGroup, Seq(config1), RateLimitMode.Enforcing)
      val rateLimit2 = RateLimit(endpointGroup, Seq(config2), RateLimitMode.Enforcing)
      val rateLimit3 = RateLimit(endpointGroup, Seq(config3), RateLimitMode.Probing)
      val rateLimit4 = RateLimit(endpointGroup, Seq(config4), RateLimitMode.Disabled)

      val rateLimitIdentity1 = RateLimitIdentity.from(config1, rateLimit1.group, rateLimit1.mode)
      val rateLimitIdentity2 = RateLimitIdentity.from(config2, rateLimit2.group, rateLimit2.mode)

      val individualRateLimiter1 = mock[IndividualRateLimiter]
      val individualRateLimiter2 = mock[IndividualRateLimiter]
      val individualRateLimiter3 = mock[IndividualRateLimiter]
      val individualRateLimiter4 = mock[IndividualRateLimiter]

      individualRateLimiter1.rateLimit returns rateLimit1
      individualRateLimiter2.rateLimit returns rateLimit2
      individualRateLimiter3.rateLimit returns rateLimit3
      individualRateLimiter4.rateLimit returns rateLimit4

      val rateLimiter = new RateLimiter("default", Seq(individualRateLimiter1, individualRateLimiter2))
      val request = mock[Request]
      val clientApplication = ClientApplication(Urn("soundcloud", "applications", "test"))
      request.headers() returns new DefaultHttpHeaders()
    }

    "return current status composed of all visible individual statuses" in new Context {
      val status1 = RateLimitStatus(rateLimitIdentity1, 10000, None)
      val status2 = RateLimitStatus(rateLimitIdentity2, 100, None)
      individualRateLimiter1.rateLimitStatus(clientApplication) returns Future(status1)
      individualRateLimiter2.rateLimitStatus(clientApplication) returns Future(status2)

      val limiter = new RateLimiter("default", Seq(
        individualRateLimiter1, individualRateLimiter2, individualRateLimiter3, individualRateLimiter4))

      Await.result(limiter.currentStatus(clientApplication)) ==== CompositeRateLimitStatus(Set(status1, status2))
    }

    "apply to a request if at least one constituent applies to it" in new Context {
      individualRateLimiter1.appliesTo(request) returns false
      individualRateLimiter2.appliesTo(request) returns true
      rateLimiter.appliesTo(request) must beTrue
    }

    "not apply to a request if at no constituent applies to it" in new Context {
      individualRateLimiter1.appliesTo(request) returns false
      individualRateLimiter2.appliesTo(request) returns false
      rateLimiter.appliesTo(request) must beFalse
    }

    "not apply to a request if no rate limit is enabled" in new Context {
      individualRateLimiter1.appliesTo(request) returns true
      individualRateLimiter2.appliesTo(request) returns true
      individualRateLimiter1.rateLimit returns rateLimit1.copy(mode = RateLimitMode.Disabled)
      individualRateLimiter2.rateLimit returns rateLimit1.copy(mode = RateLimitMode.Disabled)
      rateLimiter.appliesTo(request) must beFalse
    }

    "not advance any constituent rate limits if at least one of them has already been reached" in new Context {
      individualRateLimiter1.appliesTo(request) returns true
      individualRateLimiter2.appliesTo(request) returns true
      val status1 = RateLimitStatus(RateLimitIdentity.from(config1, rateLimit1.group, rateLimit1.mode), 10000, Some(Time.now))
      val status2 = RateLimitStatus.reached(RateLimitIdentity.from(config2, rateLimit2.group, rateLimit2.mode), Some(Time.now))
      individualRateLimiter1.rateLimitStatus(clientApplication) returns Future.value(status1)
      individualRateLimiter2.rateLimitStatus(clientApplication) returns Future.value(status2)
      val result = Await.result(rateLimiter.advanceRateLimitStatus(clientApplication, request))
      result ==== CompositeRateLimitStatus(Set(status1, status2))
      result.hasReachedLimit must beTrue
      there was no(individualRateLimiter1).advanceRateLimitStatus(clientApplication)
      there was no(individualRateLimiter2).advanceRateLimitStatus(clientApplication)
    }

    "advance all constituent rate limits if none one of them has already been reached" in new Context {
      individualRateLimiter1.appliesTo(request) returns true
      individualRateLimiter2.appliesTo(request) returns true
      val oldStatus1 = RateLimitStatus(rateLimitIdentity1, 10000, Some(Time.now))
      val oldStatus2 = RateLimitStatus(rateLimitIdentity2, 100, Some(Time.now))
      val newStatus1 = RateLimitStatus(rateLimitIdentity1, 10001, Some(Time.now))
      val newStatus2 = RateLimitStatus.reached(rateLimitIdentity2, Some(Time.now))
      individualRateLimiter1.rateLimitStatus(clientApplication) returns Future.value(oldStatus1)
      individualRateLimiter2.rateLimitStatus(clientApplication) returns Future.value(oldStatus2)
      individualRateLimiter1.advanceRateLimitStatus(clientApplication) returns Future.value(newStatus1)
      individualRateLimiter2.advanceRateLimitStatus(clientApplication) returns Future.value(newStatus2)
      val result = Await.result(rateLimiter.advanceRateLimitStatus(clientApplication, request))
      result ==== CompositeRateLimitStatus(Set(newStatus1, newStatus2))
      result.hasReachedLimit must beTrue
    }
  }
}
