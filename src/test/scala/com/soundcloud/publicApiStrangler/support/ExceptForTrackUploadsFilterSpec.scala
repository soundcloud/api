package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsObject, JsString, Json}

class ExceptForTrackUploadsFilterSpec extends UnitSpecification {
  class MyFilter extends SimpleFilter[Request, RouterResponse] {
    override def apply(request: Request, next: Service[Request, RouterResponse]) =
      next(request).map(resp => RouterResponse(new ResponseBuilder().body("filtered!").build, "/foo"))
  }

  class StubService extends Service[Request, RouterResponse] {
    override def apply(request: Request): Future[RouterResponse] =
      Future.value(RouterResponse(new ResponseBuilder().body("original!").build, "/foo"))
  }

  trait Context extends VerifiedMocks {
    val next = new StubService
    val filter = new ExceptForTrackUploadsFilter(new MyFilter)

    val filteredBody = "filtered!"
    val unfilteredBody = "original!"
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
