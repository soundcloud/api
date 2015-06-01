package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.Urn
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, Await, Time}
import org.jboss.netty.handler.codec.http.DefaultHttpHeaders
import org.joda.time.Period

class CompositeRateLimiterSpec extends UnitSpecification {

  "CompositeRateLimiter" should {

    trait Context extends Scope {
      val rateLimiter = mock[RateLimiter]
      val compositeRateLimiter = new CompositeRateLimiter(rateLimiter)
      val request = mock[Request]
      val apiClient = ApiClient(Urn("soundcloud", "applications", "test"))
      request.headers() returns new DefaultHttpHeaders()
    }

    "delegate calls to `appliesTo` to its `RateLimiter` instance" in new Context {
      rateLimiter.appliesTo(request) returns true
      compositeRateLimiter.appliesTo(request) must beTrue
      there was one(rateLimiter).appliesTo(request)
    }

    "delegate calls to `advanceRateLimitStatus` to its `RateLimiter` instance and return a 'reached' status" in new Context {
      val status = RateLimitStatus.reached(RateLimit.General(Period.hours(24), 15000), Some(Time.now))
      rateLimiter.advanceRateLimitStatus(apiClient) returns Future.value(status)
      val result = Await.result(compositeRateLimiter.advanceRateLimitStatus(apiClient))
      result ==== CompositeRateLimitStatus(Set(status))
      result.hasReachedLimit must beTrue
      there was one(rateLimiter).advanceRateLimitStatus(apiClient)
    }

    "delegate calls to `advanceRateLimitStatus` to its `RateLimiter` instance and return an 'advancing' status" in new Context {
      val status = RateLimitStatus(RateLimit.General(Period.hours(24), 15000), 10000, Some(Time.now))
      rateLimiter.advanceRateLimitStatus(apiClient) returns Future.value(status)
      val result = Await.result(compositeRateLimiter.advanceRateLimitStatus(apiClient))
      result ==== CompositeRateLimitStatus(Set(status))
      result.hasReachedLimit must beFalse
      there was one(rateLimiter).advanceRateLimitStatus(apiClient)
    }


  }

}
