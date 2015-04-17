package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.security.AuthenticatorService
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{CookieMap, Request, Response}
import com.twitter.util.{Await, Time, Future}
import com.twitter.util.TimeConversions._
import org.jboss.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}
import org.specs2.time.NoTimeConversions
import play.api.libs.json._

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
      val mockResponse = new ResponseBuilder().build
      val mockSession = mock[UserSession]
      mockSession.getAgent returns Urn("soundcloud", "applications", "mockagent")
      val mockRateLimiterProvider = mock[RateLimiterProvider]
      mockRateLimiterProvider.rateLimiters returns Set(mockRateLimiter)
      val filter = new RateLimitingFilter(mockRateLimiterProvider, mockUserAuthentication)
    }

    "let requests pass through to the service when the client hasn't reached their limit" in new Context {
      mockRateLimiter.advanceRateLimitStatus(any[ApiClient]) returns Future.value(RateLimitStatus.Advancing(20, Some(expiry)))
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value((Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "respond with 429 and reset info if the client has reached their limit" in new Context {
      mockRateLimiter.advanceRateLimitStatus(any[ApiClient]) returns Future.value(RateLimitStatus.Reached(30, Some(expiry)))
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value((Some("inconsequential"), mockSession))

      val response = Await.result(filter.apply(mockRequest, next))

      response.statusCode ==== 429
      val json = Json.parse(response.getContentString()).as[JsObject]
      json \ "rate_limit_status" ==== JsString("reached")
      json \ "max_nr_of_requests" ==== JsNumber(30)
      json \ "reset_time" ==== Json.toJson(expiry)(RateLimitStatus.timeWrites)
      there was no(next.apply(any))
    }
  }
}
