package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.Urn
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, Await, Time}
import org.jboss.netty.handler.codec.http.DefaultHttpHeaders
import org.joda.time.Period

class RateLimiterSpec extends UnitSpecification {

  "RateLimiter" should {

    trait Context extends Scope {
      val rateLimit1 = RateLimit.General(Period.hours(24), 15000)
      val rateLimit2 = RateLimit.General(Period.hours(1), 150)
      val rateLimiter1 = mock[IndividualRateLimiter]
      val rateLimiter2 = mock[IndividualRateLimiter]
      val compositeRateLimiter = new RateLimiter(Seq(rateLimiter1, rateLimiter2))
      val request = mock[Request]
      val apiClient = ApiClient(Urn("soundcloud", "applications", "test"))
      request.headers() returns new DefaultHttpHeaders()
    }

    "apply to a request if at least one constituent applies to it" in new Context {
      rateLimiter1.appliesTo(request) returns false
      rateLimiter2.appliesTo(request) returns true
      compositeRateLimiter.appliesTo(request) must beTrue
    }

    "not apply to a request if at no constituent applies to it" in new Context {
      rateLimiter1.appliesTo(request) returns false
      rateLimiter2.appliesTo(request) returns false
      compositeRateLimiter.appliesTo(request) must beFalse
    }

    "not advance any constituent rate limits if at least one of them has already been reached" in new Context {
      rateLimiter1.appliesTo(request) returns true
      rateLimiter2.appliesTo(request) returns true
      val status1 = RateLimitStatus(rateLimit1, 10000, Some(Time.now))
      val status2 = RateLimitStatus.reached(rateLimit2, Some(Time.now))
      rateLimiter1.rateLimitStatus(apiClient) returns Future.value(status1)
      rateLimiter2.rateLimitStatus(apiClient) returns Future.value(status2)
      val result = Await.result(compositeRateLimiter.advanceRateLimitStatus(apiClient, request))
      result ==== CompositeRateLimitStatus(Set(status1, status2))
      result.hasReachedLimit must beTrue
      there was no(rateLimiter1).advanceRateLimitStatus(apiClient)
      there was no(rateLimiter2).advanceRateLimitStatus(apiClient)
    }

    "advance all constituent rate limits if none one of them has already been reached" in new Context {
      rateLimiter1.appliesTo(request) returns true
      rateLimiter2.appliesTo(request) returns true
      val oldStatus1 = RateLimitStatus(rateLimit1, 10000, Some(Time.now))
      val oldStatus2 = RateLimitStatus(rateLimit2, 100, Some(Time.now))
      val newStatus1 = RateLimitStatus(rateLimit1, 10001, Some(Time.now))
      val newStatus2 = RateLimitStatus.reached(rateLimit2, Some(Time.now))
      rateLimiter1.rateLimitStatus(apiClient) returns Future.value(oldStatus1)
      rateLimiter2.rateLimitStatus(apiClient) returns Future.value(oldStatus2)
      rateLimiter1.advanceRateLimitStatus(apiClient) returns Future.value(newStatus1)
      rateLimiter2.advanceRateLimitStatus(apiClient) returns Future.value(newStatus2)
      val result = Await.result(compositeRateLimiter.advanceRateLimitStatus(apiClient, request))
      result ==== CompositeRateLimitStatus(Set(newStatus1, newStatus2))
      result.hasReachedLimit must beTrue
    }
  }
}
