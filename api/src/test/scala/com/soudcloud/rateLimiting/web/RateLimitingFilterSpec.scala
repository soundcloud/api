package com.soudcloud.rateLimiting.web

import com.soundcloud.scalakit.test.UnitSpecification
import com.soudcloud.rateLimiting.{Consumer, RateLimit}
import org.mockito.Mockito._
import java.net.InetAddress
import com.twitter.util.{Await, Future}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.Service

class RateLimitingFilterSpec extends UnitSpecification {

  trait Context extends Scope {
    val requestIp = InetAddress.getByName("10.23.131.255")
    val response = mock[Response]

    val request = mock[Request]
    when(request.remoteAddress).thenReturn(requestIp)

    val service = mock[Service[Request, Response]]
    val rateLimit = mock[RateLimit]
    val rateLimitingFilter = new RateLimitingFilter(rateLimit)
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
      when(rateLimit.checkIfAllowed(any[Consumer])).thenReturn(Future.value(true))
      when(service.apply(request)).thenReturn(Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }

  "when exception" >> {
    "returns the service' response'" in new Context {
      when(rateLimit.checkIfAllowed(any[Consumer])).thenThrow(new IllegalArgumentException("expected"))
      when(service.apply(request)).thenReturn(Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }

  "when failed future" >> {
    "returns the service' response'" in new Context {
      when(rateLimit.checkIfAllowed(any[Consumer])).thenReturn(Future.exception(new IllegalArgumentException("expected")))
      when(service.apply(request)).thenReturn(Future.value(response))
      Await.result(rateLimitingFilter(request, service)) must be_==(response)
    }
  }
}
