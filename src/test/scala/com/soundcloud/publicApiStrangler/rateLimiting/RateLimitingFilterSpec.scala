package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.security.AuthenticatorService
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Time, Future}
import com.twitter.util.TimeConversions._
import org.jboss.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}
import org.specs2.time.NoTimeConversions

class RateLimitingFilterSpec extends UnitSpecification with NoTimeConversions {

  "The RateLimitingFilter" should {

    val expiry = Time.now + 5.minutes

    trait Context extends Scope {
      val mockRateLimiter = mock[RateLimiter]
      val mockAuthenticatorService = mock[AuthenticatorService]
      val mockUserAuthentication = new UserAuthentication(mockAuthenticatorService)
      val next = mock[Service[Request, Response]]
      val mockRequest = mock[Request]
      mockRequest.headers() returns new DefaultHttpHeaders()
      val mockResponse = mock[Response]
      next.apply(any) returns Future.value(mockResponse)
      val mockSession = mock[UserSession]
      mockSession.getAgent returns Urn("soundcloud", "applications", "mockagent")
      val filter = new RateLimitingFilter(mockRateLimiter, mockUserAuthentication)
    }

    "let requests pass through to the service when the client hasn't reached their limit" in new Context {
      mockRateLimiter.advanceRateLimitStatus(any[ApiClient]) returns Future.value(RateLimitStatus.Advancing(20, Some(expiry)))
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value((Some("inconsequential"), mockSession))
      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "respond with 429 and reset info if the client has reached their limit" in new Context {

    }

  }

}
