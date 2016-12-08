package com.soundcloud.publicApiStrangler.headers

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before

import scala.collection.JavaConversions._

class DefaultResponseHeadersFilterSpec extends UnitSpecification {

  trait Context extends Scope with Before {
    val next = mock[Service[Request, RouterResponse]]
    val filter = new DefaultResponseHeadersFilter
    val request = Request("/test.json")

    override def before: Any = {
      val response = RouterResponse(request.response, "undefined")
      when(next.apply(request)).thenReturn(Future.value(response))
    }
  }

  "adds all the headers" in new Context {
    val response = Await.result(filter(request, next))
    DefaultResponseHeaders.defaultHeaders.foreach { case(k, v) =>
      response.headerMap.get(k) ==== Some(v)
    }
  }

  trait ExistingHeadersContext extends Context {
    override def before: Any = {
      val response = RouterResponse(request.response, "undefined")
      response.headerMap.add("Access-Control-Allow-Origin", "Somewhere Else")
      when(next.apply(request)).thenReturn(Future.value(response))
    }
  }

  "doesn't duplicate headers" in new ExistingHeadersContext {
    val response = Await.result(filter(request, next))
    val noDupsAllowed = response.headerMap.iterator.map { case (key, _) => key }.filter(k => k == "Access-Control-Allow-Origin")
    noDupsAllowed.size === 1
  }

}
