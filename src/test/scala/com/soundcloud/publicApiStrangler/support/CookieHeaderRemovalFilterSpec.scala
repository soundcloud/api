package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}

class CookieHeaderRemovalFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, RouterResponse]]
    val enabled: () => Future[Boolean]

    val request = Request("/tracks/123/stream.json")
    request.headerMap.set("Cookie", "sc_anonymous_id=111111-222222-333333-444444;")

    lazy val filter = new CookieHeaderRemovalFilter(enabled)
    when(next.apply(request)).thenReturn(Future.value(mock[RouterResponse]))
  }

  trait EnabledContext extends Context {
    val enabled = () => Future.True
  }

  trait DisabledContext extends Context {
    val enabled = () => Future.False
  }

  "when enabled" >> {
    "it removes the Cookie header" in new EnabledContext {
      Await.result(filter(request, next))
      request.headerMap.contains("Cookie") ==== false
    }
  }

  "when disabled" >> {
    "it leaves the Cookie header intact" in new DisabledContext {
      Await.result(filter(request, next))
      request.headerMap.contains("Cookie") ==== true
    }
  }
}
