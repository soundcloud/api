package com.soundcloud.apipublic.filter

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.applications.CreatorSubscriptionsClient
import com.soundcloud.apipublic.client.gatewayadmin.{GatewayAdminApplication, GatewayAdminClient}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRouterBuilder, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, HttpResponseFields, HttpServiceError}
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class ModifyingEndpointSubscriptionFilterSpec extends UnitSpecification {
  trait Context extends Scope {
    val userUrn = Urn("soundcloud", "users", "1")
    val appUrn = Urn("soundcloud", "applications", "999")
    val session = new UserSessionBuilder()
      .setUser(userUrn)
      .setAgent(appUrn)
      .build()
    val userAuthentication = new FakeUserAuthentication(session)
    val gatewayAdminClient = mock[GatewayAdminClient]
    val creatorSubscriptionsClient = mock[CreatorSubscriptionsClient]
    val service = mock[Service[Request, Response]]

    val router = HandlerRouterBuilder()
      .register(Method.Get, "/tracks/:trackId", _ => Future.value(JsonResponseBuilder.ok()))
      .register(Method.Post, "/tracks", _ => Future.value(JsonResponseBuilder.ok()))
      .register(Method.Post, "/likes/tracks/:trackId", _ => Future.value(JsonResponseBuilder.ok()))
      .register(Method.Post, Routing.grantExchangePath, _ => Future.value(JsonResponseBuilder.ok()))
      .build

    val filter = new ModifyingEndpointSubscriptionFilter(
      userAuthentication,
      gatewayAdminClient,
      creatorSubscriptionsClient,
      router
    )

    def metadata(accessLabel: Option[String]): GatewayAdminApplication =
      GatewayAdminApplication(accessLabel)

    def stubGatewayApplication(accessLabel: Option[String]): Unit =
      gatewayAdminClient.getApplication(session, appUrn) returns Future.value(
        Good(metadata(accessLabel))
      )

    def execute(method: Method, path: String): Response = {
      val request = Request(method, path)
      request.authorization = "OAuth token"
      Await.result(filter.apply(request, service))
    }
  }

  "allows unprotected endpoints without checking subscription" in new Context {
    service.apply(any) returns Future.value(JsonResponseBuilder.ok())

    execute(Method.Get, "/tracks/123").status ==== Status.Ok
    there was no(gatewayAdminClient).getApplication(any, any)
    there was no(creatorSubscriptionsClient).hasActiveProUnlimited(any, any)
  }

  "allows protected endpoints for apps on the default gateway policy without checking subscription" in new Context {
    stubGatewayApplication(Some("default"))
    service.apply(any) returns Future.value(JsonResponseBuilder.ok())

    execute(Method.Post, "/tracks").status ==== Status.Ok
    there was no(creatorSubscriptionsClient).hasActiveProUnlimited(any, any)
  }

  "allows protected endpoints when the app is on the low gateway policy and the user has Pro Unlimited" in new Context {
    stubGatewayApplication(Some("low"))
    creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
    service.apply(any) returns Future.value(JsonResponseBuilder.ok())

    execute(Method.Post, "/tracks").status ==== Status.Ok
    execute(Method.Post, "/likes/tracks/123").status ==== Status.Ok
  }

  "returns forbidden when the app is on the low gateway policy and the user lacks Pro Unlimited" in new Context {
    stubGatewayApplication(Some("low"))
    creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(false))

    val response = execute(Method.Post, "/tracks")
    response.status ==== Status.Forbidden
    (Json.parse(response.contentString) \ "message").as[String] must contain("Pro Unlimited")
    there was no(service).apply(any)
  }

  "allows protected endpoints when the app has no gateway access label" in new Context {
    stubGatewayApplication(None)
    service.apply(any) returns Future.value(JsonResponseBuilder.ok())

    execute(Method.Post, "/tracks").status ==== Status.Ok
    there was no(creatorSubscriptionsClient).hasActiveProUnlimited(any, any)
  }

  "returns internal server error when subscription lookup fails" in new Context {
    stubGatewayApplication(Some("low"))
    creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(
      Bad(HttpServiceError(HttpResponseFields(Status.BadGateway.code)))
    )

    execute(Method.Post, "/tracks").status ==== Status.InternalServerError
    there was no(service).apply(any)
  }

  "returns internal server error when gateway admin lookup fails" in new Context {
    gatewayAdminClient.getApplication(session, appUrn) returns Future.value(
      Bad(HttpServiceError(HttpResponseFields(Status.BadGateway.code)))
    )

    execute(Method.Post, "/tracks").status ==== Status.InternalServerError
    there was no(creatorSubscriptionsClient).hasActiveProUnlimited(any, any)
    there was no(service).apply(any)
  }

  "does not check subscription for oauth token exchange" in new Context {
    service.apply(any) returns Future.value(JsonResponseBuilder.ok())

    val request = Request(Method.Post, Routing.grantExchangePath)
    Await.result(filter.apply(request, service)).status ==== Status.Ok
    there was no(gatewayAdminClient).getApplication(any, any)
    there was no(creatorSubscriptionsClient).hasActiveProUnlimited(any, any)
  }
}
