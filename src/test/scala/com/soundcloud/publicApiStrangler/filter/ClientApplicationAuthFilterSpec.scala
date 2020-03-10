package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouterBuilder, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.{Await, Future}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ClientApplicationAuthFilterSpec extends Specification with Mockito {
  trait Context extends Scope {
    val service = mock[Service[Request, Response]]
    val telemetry = Telemetry.createIsolatedInstance
    val router = HandlerRouterBuilder()
      .register(Method.Get, "/foo", (_) => Future.value(JsonResponseBuilder.ok()))
      .build
    val sessionBuilder = new UserSessionBuilder()
    val request = HandlerRequest(Request(Method.Get, s"/foo"))
  }

  "with non blacklisted client application id" >> {
    "forwards the request" in new Context {
      service.apply(request) returns Future.value(Response(Status.Ok))

      val session = sessionBuilder.setAgent(new Urn("soundcloud", "application", "999")).build()
      val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

      Await.result(filter.apply(request, service)).status ==== Status.Ok
    }
  }

  "with missing client application id" >> {
    "forwards the request" in new Context {
      service.apply(request) returns Future.value(Response(Status.Ok))

      val session = sessionBuilder.build()
      val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

      Await.result(filter.apply(request, service)).status ==== Status.Ok
    }
  }

  "with blacklisted client application id" >> {
    "returns forbudden" in new Context {
      ClientApplicationAuthFilter.blackistedApplicationIds.foreach { appId =>
        val session = sessionBuilder.setAgent(new Urn("soundcloud", "application", appId)).build()
        val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

        Await.result(filter.apply(request, service)).status ==== Status.Forbidden
      }
    }.pendingUntilFixed("not activated yet")
  }
}
