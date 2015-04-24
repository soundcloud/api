package com.soundcloud.publicApiStrangler.headers

import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}

class DefaultResponseHeadersFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, Response]]
    val filter = new DefaultResponseHeadersFilter
    val request = Request("/test.json")

    override def before = {
      when(next.apply(request)).thenReturn(Future.value(request.response))
    }
  }

  "adds all the headers" in new Context {
    val response = Await.result(filter(request, next))
    DefaultResponseHeaders.defaultHeaders.foreach { case(k, v) =>
      response.headers().get(k) === v
    }
  }

}
