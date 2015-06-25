package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.security.{AuthenticatorService, CacheKeyAndSession}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.features.{Features, Rollout}
import com.soundcloud.publicApiStrangler.standards.PublicApiStandards._
import com.soundcloud.ratelimiting.core._
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
      val rateLimitConfiguration = RateLimitConfiguration(Bucket.ByClient, Period.seconds(2), 30)
      val rateLimit = RateLimit(EndpointGroup("global", ".*".r), Seq(rateLimitConfiguration), RateLimitMode.Enforcing)
      val rateLimitIdentity = RateLimitIdentity.from(rateLimitConfiguration, rateLimit.group, rateLimit.mode)

      val mockRateLimiterRegistry = mock[RateLimiterRegistry]
      val mockRateLimiter = mock[RateLimiter]

      mockRateLimiterRegistry.lookup(any) returns Future.value(mockRateLimiter)
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

      val filter = new RateLimitingFilter(mockRateLimiterRegistry, mockUserAuthentication, mockRollout)

      mockRollout.isActive(===(Features.WireRateLimits)) returns true
    }

    "let requests pass through if wire ratelimits flag is not active" in new Context {
      mockRollout.isActive(===(Features.WireRateLimits)) returns false
      mockRequest.path returns "/some-path"
      next.apply(any) returns Future.value(mockResponse)

      there was no (mockAuthenticatorService).cacheKeyAndSessionFor(any, any)
      there was no(mockRateLimiter).advanceRateLimitStatus(any, any)
      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "let requests pass through if probe ratelimits flag is not active for client" in new Context {
      mockRollout.isActiveForId(===(Features.ProbeRateLimits), any) returns false
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      mockRequest.path returns "/some-path"
      next.apply(any) returns Future.value(mockResponse)

      there was no(mockRateLimiter).advanceRateLimitStatus(any, any)
      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "let requests pass through if enforce ratelimits flag is not active for client" in new Context {
      mockRollout.isActiveForId(===(Features.ProbeRateLimits), any) returns true
      mockRollout.isActiveForId(===(Features.EnforceRateLimits), any) returns false
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      mockRateLimiter.advanceRateLimitStatus(any[ActionableAccessMechanism], any[Request]) returns Future.value(
        CompositeRateLimitStatus(Set(RateLimitStatus(rateLimitIdentity, 20, Some(expiry)))))
      mockRequest.path returns "/some-path"

      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "let requests pass through to the service when the client hasn't reached their limit" in new Context {
      mockRollout.isActiveForId(any, any) returns true
      mockRateLimiter.advanceRateLimitStatus(any[ActionableAccessMechanism], any[Request]) returns Future.value(
        CompositeRateLimitStatus(Set(RateLimitStatus(rateLimitIdentity, 2, Some(expiry)))))
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      mockRequest.path returns "/some-path"

      Await.result(filter.apply(mockRequest, next)) ==== mockResponse
    }

    "respond with 429 and reset info if the client has reached their limit" in new Context {
      mockRollout.isActiveForId(any, any) returns true
      mockRateLimiter.advanceRateLimitStatus(any[ActionableAccessMechanism], any[Request]) returns Future.value(
        CompositeRateLimitStatus(Set(RateLimitStatus.reached(rateLimitIdentity, Some(expiry)))))
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      mockRequest.path returns "/some-path"

      val response = Await.result(filter.apply(mockRequest, next))

      response.statusCode ==== 429
      val json = Json.parse(response.getContentString()).as[JsObject]
      val errors = json \ "errors" \\ "meta"
      errors must haveSize(1)
      val meta = errors.head
      meta \ "rate_limit" ==== Json.obj(
        "max_nr_of_requests" -> 30,
        "time_window" -> "PT2S",
        "group" -> "global"
      )
      meta \ "reset_time" ==== Json.toJson(expiry)
      meta \ "remaining_requests" ==== JsNumber(0)
      there was no(next).apply(any)
    }
    
    "call the next service if the route is an internal one" in new Context {
      next.apply(any) returns Future.value(mockResponse)
      mockRequest.path returns "/-/health"

      val result = Await.result(filter.apply(mockRequest, next))

      there was no(mockAuthenticatorService).cacheKeyAndSessionFor(any, any)
      result ==== mockResponse
    }

    "call the next service if the ratelimiter does not apply to the request route" in new Context {
      mockAuthenticatorService.cacheKeyAndSessionFor(any, any) returns Future.value(CacheKeyAndSession(Some("inconsequential"), mockSession))
      next.apply(any) returns Future.value(mockResponse)
      mockRequest.path returns "/notapplicable"
      mockRateLimiter.appliesTo(mockRequest).returns(false)

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

    "call the next service in case of unexpected errors, not caused by downstream" should {
      "error in the context of future" in new Context {
        val newFilter = new RateLimitingFilter(mockRateLimiterRegistry, mockUserAuthentication, mockRollout) {
          override def rateLimitedResponse(request: Request): Future[Option[Response]] = {
            Future.exception(new RuntimeException)
          }
        }

        next.apply(any) returns Future.value(mockResponse)
        mockRequest.path returns "/some-path"

        val result = Await.result(filter.apply(mockRequest, next))
        result ==== mockResponse
      }

      "error outside the context of future" in new Context {
        val newFilter = new RateLimitingFilter(mockRateLimiterRegistry, mockUserAuthentication, mockRollout) {
          override def rateLimitedResponse(request: Request): Future[Option[Response]] = {
            throw new RuntimeException
          }
        }

        next.apply(any) returns Future.value(mockResponse)
        mockRequest.path returns "/some-path"

        val result = Await.result(filter.apply(mockRequest, next))
        result ==== mockResponse
      }
    }
  }
}
