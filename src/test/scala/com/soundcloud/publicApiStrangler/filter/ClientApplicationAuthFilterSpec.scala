package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouterBuilder, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.Routing
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
      .register(Method.Get, "/foo", _ => Future.value(JsonResponseBuilder.ok()))
      .register(Method.Post, Routing.grantExchangePath, _ => Future.value(JsonResponseBuilder.ok()))
      .build
    val sessionBuilder = new UserSessionBuilder()
    val request = HandlerRequest(Request(s"/foo", ("client_id", "999")))
  }

  "with allowlisted client application id" >> {
    "forwards the request" in new Context {
      service.apply(request) returns Future.value(Response(Status.Ok))

      val session = sessionBuilder.setAgent(new Urn("soundcloud", "application", "999")).build()
      val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

      Await.result(filter.apply(request, service)).status ==== Status.Ok
    }

    "logs client param" in new Context {
      service.apply(request) returns Future.value(Response(Status.Ok))

      val session = sessionBuilder.setAgent(new Urn("soundcloud", "application", "999")).build()
      val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

      Await.result(filter.apply(request, service)).status

      telemetry.getSampleValue("application_auth_type_total", Seq("auth_type", "path"), Seq("client_id_param", "/foo")) === Some(
        1
      )
      telemetry.getSampleValue("deprecated_auth_by_app_total", Seq("appid"), Seq("999")) === Some(1)
    }
  }

  "with token exchange request" >> {
    trait TokenExchangeContext extends Context {
      override val request = HandlerRequest(Request(Method.Post, "/oauth2/token"))
    }

    "forwards the request" in new TokenExchangeContext {
      service.apply(request) returns Future.value(Response(Status.Ok))

      ClientApplicationAuthFilter.blockedApplicationIds.foreach { appId =>
        val session = sessionBuilder.setAgent(new Urn("soundcloud", "application", appId)).build()
        val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

        Await.result(filter.apply(request, service)).status ==== Status.Ok
      }
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

  "with denylisted client application id" >> {
    "returns forbidden" in new Context {
      ClientApplicationAuthFilter.blockedApplicationIds.foreach { appId =>
        val session = sessionBuilder.setAgent(new Urn("soundcloud", "application", appId)).build()
        val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

        Await.result(filter.apply(request, service)).status ==== Status.Forbidden
      }
    }
  }

  "with oauth header" >> {
    "logs auth type but not deprecated id" in new Context {
      private val r: Request = Request("/foo")
      r.authorization = "OAuth 1234"
      override val request = HandlerRequest(r)
      service.apply(request) returns Future.value(Response(Status.Ok))

      val session = sessionBuilder.setAgent(new Urn("soundcloud", "application", "999")).build()
      val filter = new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

      Await.result(filter.apply(request, service)).status

      telemetry.getSampleValue("application_auth_type_total", Seq("auth_type", "path"), Seq("oauth_header", "/foo")) === Some(
        1
      )
      telemetry.getSampleValue("deprecated_auth_by_app_total", Seq("appid"), Seq("999")) === None

    }
  }
}
