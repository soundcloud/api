package com.soundcloud.publicApiStrangler

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Method
import com.soundcloud.jvmkit.module.http.server.Handler

class RoutingSpec extends UnitSpecification {
  trait Context extends Scope {
    lazy val handler = mock[Handler]
  }

  "generate path combinations" in new Context {
    Routing.route(Method.Get, "/path", handler) mustEqual List(
      (Method.Get, "/path", handler),
      (Method.Get, "/path/", handler),
      (Method.Get, "/path.json", handler),
      (Method.Get, "/path.json/", handler)
    )
  }

  "generate path combinations with an id" in new Context {
    Routing.route(Method.Get, "/path/:id", handler) mustEqual List(
      (Method.Get, "/path/:id", handler),
      (Method.Get, "/path/:id/", handler),
      (Method.Get, "/path/:id.json", handler),
      (Method.Get, "/path/:id.json/", handler)
    )
  }
}
