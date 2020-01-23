package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.{Await, Future}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class HeadRequestFilterSpec extends Specification {
  val bodyMessage = "body"

  class StubService extends Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      Future.value(JsonResponseBuilder.ok(bodyMessage))
    }
  }

  trait Context extends Scope {
    val filter = new HeadRequestFilter
  }

  "GET request" >> {
    trait GetRequestContext extends Context {
      val service = new StubService
      val request = Request(Method.Get, "/")
    }

    "does not manipulate the request and response" in new GetRequestContext {
      val response = Await.result(filter(request, service))
      response.contentString ==== bodyMessage
    }
  }

  "HEAD request" >> {
    trait GetRequestContext extends Context {
      val service = new StubService
      val request = Request(Method.Head, "/")
    }

    "manipulate the request and response" in new GetRequestContext {
      val response = Await.result(filter(request, service))
      response.contentString ==== ""
    }
  }

}
