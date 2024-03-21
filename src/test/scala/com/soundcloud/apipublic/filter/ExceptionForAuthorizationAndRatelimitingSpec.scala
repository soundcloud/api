package com.soundcloud.apipublic.filter

import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Await, Future}

class ExceptionForAuthorizationAndRatelimitingSpec extends UnitSpecification {
  class MyFilter extends SimpleFilter[Request, Response] {
    override def apply(request: Request, next: Service[Request, Response]) =
      next(request).map(resp => ResponseBuilder().body("filtered!").build)
  }

  class StubService extends Service[Request, Response] {
    override def apply(request: Request): Future[Response] =
      Future.value(ResponseBuilder().body("original!").build)
  }

  trait Context extends Scope {
    val next = new StubService
    val filter = new ExceptionForAuthorizationAndRatelimiting(new MyFilter)

    val filteredBody = "filtered!"
    val unfilteredBody = "original!"
  }

  "it does not run filter for POST /muzooka/webhook" in new Context {
    Await.result(filter(Request(Method.Post, "/muzooka/webhook"), next)).contentString ==== unfilteredBody
  }

  "it does not run filter for POST /tracks" in new Context {
    Await.result(filter(Request(Method.Post, "/tracks"), next)).contentString ==== unfilteredBody
  }

  "it does not run filter for PUT /tracks/:id" in new Context {
    Await.result(filter(Request(Method.Put, "/tracks/123"), next)).contentString ==== unfilteredBody
    Await.result(filter(Request(Method.Put, "/tracks/soundcloud:tracks:123"), next)).contentString ==== unfilteredBody
  }

  "it runs run filter for everything else" in new Context {
    Await.result(filter(Request(Method.Post, "/something/tracks"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Post, "/tracks/123"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Post, "/tracks/soundcloud:tracks:123"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Post, "/users"), next)).contentString ==== filteredBody

    Await.result(filter(Request(Method.Get, "/tracks"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Get, "/tracks/123"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Get, "/tracks/soundcloud:tracks:123"), next)).contentString ==== filteredBody

    Await.result(filter(Request(Method.Put, "/tracks"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Put, "/something/tracks/123"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Put, "/users"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Put, "/users/123"), next)).contentString ==== filteredBody
    Await.result(filter(Request(Method.Put, "/users/soundcloud:users:123"), next)).contentString ==== filteredBody
  }
}
