package com.soudcloud.rateLimiting.web

import com.soundcloud.scalakit.test.UnitSpecification
import com.soudcloud.rateLimiting.{Consumer, RateLimit}
import org.mockito.Mockito._
import com.twitter.util.{Await, Future}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http._
import com.soudcloud.rateLimiting.Ip

class RateLimitingFilterSpec extends UnitSpecification {

  trait Context extends Scope {
    val requestIp = Ip("10.23.131.255")
    val response = Response(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))


    val service = mock[Service[Request, Response]]
    val rateLimit = mock[RateLimit]
    val rateLimitingFilter = new RateLimitingFilter(rateLimit)

    val request: Request = {
      val httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/")
      httpRequest.headers.set(rateLimitingFilter.realIpHeader, requestIp.address)
      Request(httpRequest)
    }
  }

  "when rate limited" >> {
    trait RateLimitedCall extends Context {
      when(rateLimit.checkIfAllowed(any[Consumer])).thenReturn(Future.value(false))

      val actualResponse = Await.result(rateLimitingFilter(request, service))
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
      service.apply(request) returns (Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }

  "when exception" >> {
    "returns the service' response'" in new Context {
      rateLimit.checkIfAllowed(requestIp) throws (new IllegalArgumentException("expected"))
      service.apply(request) returns (Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }

  "when no real IP" >> {
    "returns '400 Bad Request'" in new Context {
      val requestWithoutIp = Request(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/"))

      Await.result(rateLimitingFilter(requestWithoutIp, service)).statusCode must be_==(400)

      there was noCallsTo(rateLimit)
      there was noCallsTo(service)
    }
  }

  "when failed future" >> {
    "returns the service' response'" in new Context {
      rateLimit.checkIfAllowed(requestIp) returns (Future.exception(new IllegalArgumentException("expected")))
      service.apply(request) returns (Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }
}
