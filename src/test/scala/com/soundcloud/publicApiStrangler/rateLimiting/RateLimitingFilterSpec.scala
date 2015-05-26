package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.security.{CacheKeyAndSession, AuthenticatorService}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.publicApiStrangler.standards.PublicApiStandards._
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.ratelimiting.whitelisting.ApplicationLevelWhitelistProxy
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.TimeConversions._
import com.twitter.util.{Await, Future, Time}
import org.jboss.netty.handler.codec.http.DefaultHttpHeaders
import org.joda.time.Period
import org.specs2.time.NoTimeConversions
import play.api.libs.json._

class RateLimitingFilterSpec extends UnitSpecification with NoTimeConversions {

  "The RateLimitingFilter" should {

    val expiry = Time.now + 5.minutes

    trait Context extends Scope {
      val rateLimit = RateLimit.General(Period.seconds(2), 30)

      val mockRateLimiter = mock[RateLimiter]
      mockRateLimiter.appliesTo(any).returns(true)

      val mockAuthenticatorService = mock[AuthenticatorService]

      val mockUserAuthentication = new UserAuthentication(mockAuthenticatorService)

      val next = mock[Service[Request, Response]]

      val mockRequest = mock[Request]
      mockRequest.headers() returns new DefaultHttpHeaders()
      val mockResponse = new ResponseBuilder().build

      val mockSession = mock[UserSession]
      val mockAgentUrn = Urn("soundcloud", "applications", "mockagent")
      mockSession.getAgent returns mockAgentUrn

      val mockRollout = mock[Rollout]

      val mockWhitelistingService = mock[ApplicationLevelWhitelistProxy]

      val filter = new RateLimitingFilter(mockRateLimiter, mockUserAuthentication, mockRollout, mockWhitelistingService)
    }

    "let requests pass through if probe ratelimits flag is not active for client" in new Context {
      mockRollout.isActiveForId(===(Features.ProbeRateLimits), any) returns false
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      mockRequest.path returns "/some-path"
      next.apply(any) returns Future.value(mockResponse)

      there was no(mockRateLimiter).advanceRateLimitStatus(any)
      there was no(mockWhitelistingService).hasClientWhitelisted(any)
      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "let requests pass through if enforce ratelimits flag is not active for client" in new Context {
      mockRollout.isActiveForId(===(Features.ProbeRateLimits), any) returns true
      mockRollout.isActiveForId(===(Features.EnforceRateLimits), any) returns false
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      mockRateLimiter.advanceRateLimitStatus(any[ApiClient]) returns Future.value(RateLimitStatus.Advancing(rateLimit, 20, Some(expiry)))
      mockRequest.path returns "/some-path"

      there was no(mockWhitelistingService).hasClientWhitelisted(any)
      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "let requests pass through to the service when the client hasn't reached their limit" in new Context {
      mockRollout.isActiveForId(any, any) returns true
      mockRateLimiter.advanceRateLimitStatus(any[ApiClient]) returns Future.value(RateLimitStatus.Advancing(rateLimit, 2, Some(expiry)))
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      mockWhitelistingService.hasClientWhitelisted(any) returns false
      mockRequest.path returns "/some-path"

      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "respond with 429 and reset info if the client has reached their limit" in new Context {
      mockRollout.isActiveForId(any, any) returns true
      mockRateLimiter.advanceRateLimitStatus(any[ApiClient]) returns Future.value(RateLimitStatus.Reached(rateLimit, Some(expiry)))
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      mockWhitelistingService.hasClientWhitelisted(any) returns false
      mockRequest.path returns "/some-path"

      val response = Await.result(filter.apply(mockRequest, next))

      response.statusCode ==== 429
      val json = Json.parse(response.getContentString()).as[JsObject]
      json \ "rate_limit_status" ==== JsString("reached")
      json \ "max_nr_of_requests" ==== JsNumber(30)
      json \ "reset_time" ==== Json.toJson(expiry)
      there was no(next).apply(any)
    }

    "not rate-limit the client if they are whitelisted" in new Context {
      mockRollout.isActiveForId(any, any) returns true
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      mockWhitelistingService.hasClientWhitelisted(mockAgentUrn) returns true
      mockRequest.path returns "/some-path"

      val result = Await.result(filter.apply(mockRequest, next))

      there was no(mockRateLimiter).advanceRateLimitStatus(ApiClient(mockAgentUrn))
      result ==== mockResponse
    }

    "call the next service in case of unexpected errors, not caused by downstream" in new Context {
      mockRollout.isActiveForId(any, any) throws new RuntimeException("Unexpected error.")
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      mockRequest.path returns "/some-path"

      val result = Await.result(filter.apply(mockRequest, next))
      result ==== mockResponse
    }

    "not call next again if a downstream error is propagated to the filter" in new Context {
      mockRollout.isActiveForId(any, any) returns false
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.exception(new RuntimeException)
      mockRequest.path returns "/some-path"

      def result = Await.result(filter.apply(mockRequest, next))

      result must throwA[RuntimeException]
      there was one(next).apply(any)
    }

    "call the next service if the route is an internal one" in new Context {
      next.apply(any) returns Future.value(mockResponse)
      mockRequest.path returns "/-/health"

      val result = Await.result(filter.apply(mockRequest, next))

      there was no(mockAuthenticatorService).cacheKeyAndSessionFor(any, any)
      result ==== mockResponse
    }

    "call the next service if the ratelimiter does not apply to the request route" in new Context {
      next.apply(any) returns Future.value(mockResponse)
      mockRequest.path returns "/notapplicable"
      mockRateLimiter.appliesTo(mockRequest).returns(false)

      val result = Await.result(filter.apply(mockRequest, next))

      there was no(mockAuthenticatorService).cacheKeyAndSessionFor(any, any)
      result ==== mockResponse
    }

  }
}
