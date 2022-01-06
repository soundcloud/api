package com.soundcloud.apipublic

import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.Method
import com.soundcloud.jvmkit.module.http.server.Handler

class RoutingSpec extends UnitSpecification {
  trait Context extends Scope {
    lazy val handler = mock[Handler]
  }

  "generate GET path combinations" in new Context {
    Routing.route(Method.Get, "/path", handler) mustEqual List(
      (Method.Get, "/path", handler),
      (Method.Get, "/path/", handler),
      (Method.Get, "/path.json", handler),
      (Method.Get, "/path.json/", handler),
      (Method.Get, "/v1/path", handler),
      (Method.Get, "/v1/path/", handler),
      (Method.Get, "/v1/path.json", handler),
      (Method.Get, "/v1/path.json/", handler),
      (Method.Head, "/path", handler),
      (Method.Head, "/path/", handler),
      (Method.Head, "/path.json", handler),
      (Method.Head, "/path.json/", handler),
      (Method.Head, "/v1/path", handler),
      (Method.Head, "/v1/path/", handler),
      (Method.Head, "/v1/path.json", handler),
      (Method.Head, "/v1/path.json/", handler)
    )
  }

  "generate GET path combinations with an id" in new Context {
    Routing.route(Method.Get, "/path/:id", handler) mustEqual List(
      (Method.Get, "/path/:id", handler),
      (Method.Get, "/path/:id/", handler),
      (Method.Get, "/path/:id.json", handler),
      (Method.Get, "/path/:id.json/", handler),
      (Method.Get, "/v1/path/:id", handler),
      (Method.Get, "/v1/path/:id/", handler),
      (Method.Get, "/v1/path/:id.json", handler),
      (Method.Get, "/v1/path/:id.json/", handler),
      (Method.Head, "/path/:id", handler),
      (Method.Head, "/path/:id/", handler),
      (Method.Head, "/path/:id.json", handler),
      (Method.Head, "/path/:id.json/", handler),
      (Method.Head, "/v1/path/:id", handler),
      (Method.Head, "/v1/path/:id/", handler),
      (Method.Head, "/v1/path/:id.json", handler),
      (Method.Head, "/v1/path/:id.json/", handler)
    )
  }

  "generate POST path combinations" in new Context {
    Routing.route(Method.Post, "/path", handler) mustEqual List(
      (Method.Post, "/path", handler),
      (Method.Post, "/path/", handler),
      (Method.Post, "/path.json", handler),
      (Method.Post, "/path.json/", handler),
      (Method.Post, "/v1/path", handler),
      (Method.Post, "/v1/path/", handler),
      (Method.Post, "/v1/path.json", handler),
      (Method.Post, "/v1/path.json/", handler)
    )
  }
}
