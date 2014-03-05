package com.soudcloud.rateLimiting.web

import com.soundcloud.scalakit.test.UnitSpecification
import com.soudcloud.rateLimiting.{Ip, Consumer, RateLimit}
import org.mockito.Mockito._
import com.twitter.util.{Await, Future}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.HttpHeaders

class RateLimitingFilterSpec extends UnitSpecification {

  trait Context extends Scope {
    val requestIp = Ip("10.23.131.255")
    val response = mock[Response]

    val request = mock[Request]

    val headers = mock[HttpHeaders]
    stub(request.headers).toReturn(headers)

    val service = mock[Service[Request, Response]]
    val rateLimit = mock[RateLimit]
    val rateLimitingFilter = new RateLimitingFilter(rateLimit)
  }

  "when rate limited" >> {
    trait RateLimitedCall extends Context {
      stub(headers.get("X-Real-Ip")).toReturn(requestIp.address)
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
      when(rateLimit.checkIfAllowed(requestIp)).thenReturn(Future.value(true))
      when(service.apply(request)).thenReturn(Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }

  "when exception" >> {
    "returns the service' response'" in new Context {
      when(rateLimit.checkIfAllowed(requestIp)).thenThrow(new IllegalArgumentException("expected"))
      when(service.apply(request)).thenReturn(Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }

  "when failed future" >> {
    "returns the service' response'" in new Context {
      when(rateLimit.checkIfAllowed(requestIp)).thenReturn(Future.exception(new IllegalArgumentException("expected")))
      when(service.apply(request)).thenReturn(Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }
}
