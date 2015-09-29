package com.soundcloud.publicApiStrangler.headers

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}

import scala.collection.JavaConversions._

class DefaultResponseHeadersFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, RouterResponse]]
    val filter = new DefaultResponseHeadersFilter
    val request = Request("/test.json")

    override def before = {
      val response = RouterResponse(request.response, "undefined")
      when(next.apply(request)).thenReturn(Future.value(response))
    }
  }

  "adds all the headers" in new Context {
    val response = Await.result(filter(request, next))
    DefaultResponseHeaders.defaultHeaders.foreach { case(k, v) =>
      response.headers().get(k) === v
    }
  }

  trait ExistingHeadersContext extends Context {
    override def before = {
      val response = RouterResponse(request.response, "undefined")
      response.headers().add("Access-Control-Allow-Origin", "Somewhere Else")
      when(next.apply(request)).thenReturn(Future.value(response))
    }
  }

  "doesn't duplicate headers" in new ExistingHeadersContext {
    val response = Await.result(filter(request, next))
    val noDupsAllowed = response.headers().entries().map { e => e.getKey }.filter(k => k == "Access-Control-Allow-Origin")
    noDupsAllowed.size === 1
  }

}
