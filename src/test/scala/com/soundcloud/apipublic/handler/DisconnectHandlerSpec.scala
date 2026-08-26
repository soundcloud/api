package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.TokenDispenserClient
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.twitter.finagle.http.{Method, Response, Status}

class DisconnectHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val userUrn = Urn("soundcloud", "users", "42")
    val applicationUrn = Urn("soundcloud", "applications", "123")

    val session: UserSession = new UserSessionBuilder()
      .setUser(userUrn)
      .setAgent(applicationUrn)
      .build()

    val tokenDispenserClient = mock[TokenDispenserClient]
    val exceptionCollector = mock[ExceptionCollector]

    lazy val handler = new DisconnectHandler(
      new FakeUserAuthentication(session),
      tokenDispenserClient,
      exceptionCollector
    )

    override def routingDefinitions(): List[(Method, String, com.soundcloud.jvmkit.module.http.server.Handler)] =
      Routing.forDisconnectHandler(handler)

    def disconnect(): Response = post("/disconnect")
  }

  "POST /disconnect" >> {
    "returns 401 when user is anonymous" in new Context {
      override val session: UserSession = anonymousSession
      disconnect().status ==== Status.Unauthorized
    }

    "returns 400 when session agent is not an application" in new Context {
      override val session: UserSession = new UserSessionBuilder()
        .setUser(userUrn)
        .setAgent(Urn("soundcloud", "systems", "1"))
        .build()

      disconnect().status === Status.BadRequest
    }

    "returns 204 when token invalidation succeeds for the session application" in new Context {
      tokenDispenserClient.invalidateTokensForApplication(session, applicationUrn) returns Good(()).outcomeF
      disconnect().status === Status.NoContent
    }

    "returns 500 when token invalidation fails" in new Context {
      tokenDispenserClient.invalidateTokensForApplication(session, applicationUrn) returns
        UnexpectedError(new IllegalStateException("upstream failure")).badF

      disconnect().status === Status.InternalServerError
    }
  }
}
