package com.soundcloud.publicApiStrangler.rateLimitingOld.web

import com.soundcloud.publicApiStrangler.rateLimitingOld.{Consumer, Ip, RateLimit}
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http._
import org.mockito.Mockito._

class RateLimitingFilterSpec extends UnitSpecification {

  trait Context extends Scope {
    val requestIp = Ip("10.23.131.255")
    val response = Response(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))


    val service = mock[Service[HandlerRequest, Response]]
    val rateLimit = mock[RateLimit]
    val rateLimitingFilter = new RateLimitingFilter(rateLimit)

    val request: Request = {
      val httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/")
      httpRequest.headers.set(rateLimitingFilter.realIpHeader, requestIp.address)
      Request(httpRequest)
    }

    val handlerRequest = new HandlerRequest(AlwaysMatchesPathMatcher, request)
  }

  "when rate limited" >> {
    trait RateLimitedCall extends Context {
      when(rateLimit.checkIfAllowed(any[Consumer])).thenReturn(Future.value(false))

      val actualResponse = Await.result(rateLimitingFilter(handlerRequest, service))
    }

    "returns 'HTTP 429 Too Many Requests'" in new RateLimitedCall {
      actualResponse.statusCode must be_==(429)
    }

    "does not invoke the service" in new RateLimitedCall {
      there was noCallsTo(service)
    }
  }

  "when not rate limited" >> {
    "returns the service' response'" in new Context {
      rateLimit.checkIfAllowed(requestIp) returns (Future.value(true))
      service.apply(handlerRequest) returns (Future.value(response))
      Await.result(rateLimitingFilter(handlerRequest, service)) must be_==(response)
    }
  }

  "when exception" >> {
    "returns the service' response'" in new Context {
      rateLimit.checkIfAllowed(requestIp) throws (new IllegalArgumentException("expected"))
      service.apply(handlerRequest) returns (Future.value(response))
      Await.result(rateLimitingFilter(handlerRequest, service)) must be_==(response)
    }
  }

  "when no real IP" >> {
    "returns '400 Bad Request'" in new Context {
      val requestWithoutIp = new HandlerRequest(AlwaysMatchesPathMatcher, Request(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/")))

      Await.result(rateLimitingFilter(requestWithoutIp, service)).statusCode must be_==(400)

      there was noCallsTo(rateLimit)
      there was noCallsTo(service)
    }
  }

  "when failed future" >> {
    "returns the service' response'" in new Context {
      rateLimit.checkIfAllowed(requestIp) returns (Future.exception(new IllegalArgumentException("expected")))
      service.apply(handlerRequest) returns (Future.value(response))
      Await.result(rateLimitingFilter(handlerRequest, service)) must be_==(response)
    }
  }
}
