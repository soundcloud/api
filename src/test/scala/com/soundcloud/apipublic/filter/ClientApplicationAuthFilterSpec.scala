package com.soundcloud.apipublic.filter

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouterBuilder, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.apipublic.Routing
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
      .register(Method.Get, Routing.connectPath, _ => Future.value(JsonResponseBuilder.ok()))
      .build

    lazy val session = new UserSessionBuilder().setAgent(new Urn("soundcloud", "application", "999")).build()
    lazy val request = HandlerRequest(Request("/foo", ("client_id", "999")))

    val filter =
      new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)
  }

  "with present application id" >> {
    "forwards the request if Auth header is present" in new Context {
      private val r: Request = Request("/foo")
      r.authorization_=("OAuth 1234")
      override lazy val request = HandlerRequest(r)

      service.apply(request) returns Future.value(Response(Status.Ok))

      Await.result(filter.apply(request, service)).status ==== Status.Ok
      telemetry.getSampleValue("application_auth_type_total", Seq("auth_type", "path"), Seq("oauth_header", "/foo")) === Some(
        1
      )
    }

    "logs client param and rejects the request as Auth header is not present" in new Context {
      service.apply(request) returns Future.value(Response(Status.Ok))

      val result = Await.result(filter.apply(request, service))
      result.status ==== Status.Unauthorized
      result.contentString = ClientApplicationAuthFilter.invalidAuthenticationError

      telemetry.getSampleValue("application_auth_type_total", Seq("auth_type", "path"), Seq("client_id_param", "/foo")) === Some(
        1
      )
      telemetry.getSampleValue("deprecated_auth_by_app_total", Seq("appid"), Seq("999")) === Some(1)
    }
  }

  "with token exchange request" >> {
    "forwards the request" in new Context {
      override lazy val request = HandlerRequest(Request(Method.Post, "/oauth2/token"))
      service.apply(request) returns Future.value(Response(Status.Ok))

      ClientApplicationAuthFilter.blockedApplicationIds.foreach { appId =>
        val session = new UserSessionBuilder().setAgent(new Urn("soundcloud", "application", appId)).build()
        val filter =
          new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

        Await.result(filter.apply(request, service)).status ==== Status.Ok
      }
    }
  }

  "with missing application id" >> {
    "forwards the request if Auth header is present" in new Context {
      private val r: Request = Request("/foo")
      r.authorization_=("OAuth 1234")
      override lazy val request = HandlerRequest(r)
      override lazy val session = new UserSessionBuilder().build()

      service.apply(request) returns Future.value(Response(Status.Ok))

      Await.result(filter.apply(request, service)).status ==== Status.Ok
    }

    "rejects the request if Auth header is not present" in new Context {
      override lazy val session = new UserSessionBuilder().build()

      val result = Await.result(filter.apply(request, service))
      result.status ==== Status.Unauthorized
      result.contentString = ClientApplicationAuthFilter.invalidAuthenticationError

    }
  }

  "with denylisted client application id" >> {
    "returns forbidden" in new Context {

      ClientApplicationAuthFilter.blockedApplicationIds.foreach { appId =>
        val session = new UserSessionBuilder().setAgent(new Urn("soundcloud", "application", appId)).build()
        val filter =
          new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

        Await.result(filter.apply(request, service)).status ==== Status.Forbidden
      }
    }
  }

  "with oauth header" >> {
    "forwards request, logs auth type but not deprecated id" in new Context {
      private val r: Request = Request("/foo")
      r.authorization_=("OAuth 1234")

      override lazy val request = HandlerRequest(r)
      service.apply(request) returns Future.value(Response(Status.Ok))

      Await.result(filter.apply(request, service)).status ==== Status.Ok

      telemetry.getSampleValue("application_auth_type_total", Seq("auth_type", "path"), Seq("oauth_header", "/foo")) === Some(
        1
      )
      telemetry.getSampleValue("deprecated_auth_by_app_total", Seq("appid"), Seq("999")) === None
    }

    "forwards request for allowlisted apps" in new Context {
      service.apply(request) returns Future.value(Response(Status.Ok))

      ClientApplicationAuthFilter.allowlistedApplicationIds.foreach { appId =>
        val session = new UserSessionBuilder().setAgent(new Urn("soundcloud", "application", appId)).build()
        val filter =
          new ClientApplicationAuthFilter(new FakeUserAuthentication(session), telemetry, router)

        Await.result(filter.apply(request, service)).status ==== Status.Ok
      }
    }
  }

  "/connect" >> {
    "valid response_type/scope" in new Context {
      override lazy val request =
        HandlerRequest(Request("/connect?client_id=123&response_type=code&redirect_uri=ww.example.com&scope="))

      service.apply(any[Request]) returns Future.value(Response(Status.Ok))

      Await.result(filter.apply(request, service)).status ==== Status.Ok
    }

    "invalid response_type" in new Context {
      override lazy val request =
        HandlerRequest(Request("/connect?client_id=123&response_type=token&redirect_uri=ww.example.com&scope="))

      val result = Await.result(filter.apply(request, service))

      result.status ==== Status.Forbidden
      result.contentString = ClientApplicationAuthFilter.invalidResponseTypeError
    }

    "invalid scope" in new Context {
      override lazy val request =
        HandlerRequest(
          Request("/connect?client_id=123&response_type=code&redirect_uri=ww.example.com&scope=non-expiring")
        )

      val result = Await.result(filter.apply(request, service))

      result.status ==== Status.Forbidden
      result.contentString = ClientApplicationAuthFilter.invalidScopeError
    }
  }
}
