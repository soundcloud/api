package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.ratelimiting.core.{RateLimitMode, ClientApplication, EndpointGroup, RateLimit}
import com.soundcloud.scalakit.Urn
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, Await, Time}
import org.jboss.netty.handler.codec.http.DefaultHttpHeaders
import org.joda.time.Period

class RateLimiterSpec extends UnitSpecification {

  "RateLimiter" should {

    trait Context extends Scope {
      val endpointGroup = EndpointGroup("default", ".*".r)
      val rateLimit1 = RateLimit(endpointGroup, Period.hours(24), 15000, RateLimitMode.Probing)
      val rateLimit2 = RateLimit(endpointGroup, Period.hours(1), 150, RateLimitMode.Probing)
      val individualRateLimiter1 = mock[IndividualRateLimiter]
      val individualRateLimiter2 = mock[IndividualRateLimiter]
      val rateLimiter = new RateLimiter("default", Seq(individualRateLimiter1, individualRateLimiter2))
      val request = mock[Request]
      val clientApplication = ClientApplication(Urn("soundcloud", "applications", "test"))
      request.headers() returns new DefaultHttpHeaders()
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

    "not advance any constituent rate limits if at least one of them has already been reached" in new Context {
      individualRateLimiter1.appliesTo(request) returns true
      individualRateLimiter2.appliesTo(request) returns true
      val status1 = RateLimitStatus(rateLimit1, 10000, Some(Time.now))
      val status2 = RateLimitStatus.reached(rateLimit2, Some(Time.now))
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
      val oldStatus1 = RateLimitStatus(rateLimit1, 10000, Some(Time.now))
      val oldStatus2 = RateLimitStatus(rateLimit2, 100, Some(Time.now))
      val newStatus1 = RateLimitStatus(rateLimit1, 10001, Some(Time.now))
      val newStatus2 = RateLimitStatus.reached(rateLimit2, Some(Time.now))
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
