package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.{Method, Status}
import com.twitter.util.Future

class DispatchingToMotherShipSpec extends UnitSpecification {
  val expectedMotherShipEndpoints = Set(
    (Post, "/playlists"),
    (Put, "/playlists/1"),
    (Get, "/resolve")
  )

  val expectedUserRelatedMothershipEndpoint = Set(
    (Get, "/users/7110"),
    (Get, "/users/7110/comments"),
    (Get, "/users/me"),
    (Get, "/users/suggested"),
    (Get, "/tracks/7110/favoriters"),
    (Get, "/me")
  )

  trait MothershipContext extends HandlerSpecificationScope {
    val dispatcher = mock[DispatchToMothershipHandler]
    dispatcher.dispatch(any[HandlerRequest]) returns Future.value(ResponseBuilder.ok())

    override def routingDefinitions() = Routing.forMothershipDispatcher(dispatcher)
  }

  trait TokenExchangeContext extends HandlerSpecificationScope {
    override def routingDefinitions() = Routing.forTokenExchange(_ => Future(ResponseBuilder.ok()))
  }

  trait UserRelatedMothershipContext extends HandlerSpecificationScope {
    val dispatcher = mock[UserRelatedMothershipDispatcher]
    dispatcher.dispatchToMothership(any[HandlerRequest]) returns Future.value(ResponseBuilder.ok())

    override def routingDefinitions() = Routing.forUserRelatedMothershipDispatcher(dispatcher)
  }

  expectedMotherShipEndpoints foreach {
    case (method, endpoint) =>
      s"Mothership dispatcher should handle $method at $endpoint" in new MothershipContext {
        method match {
          case Method.Get => get(endpoint).status = Status.Ok
          case Method.Post => post(endpoint).status = Status.Ok
          case Method.Put => put(endpoint).status = Status.Ok
          case m => throw new UnsupportedOperationException(s"Test for method $m not implemented")
        }
      }
  }

  "Mothership dispatcher should handle POST to token exchange /oauth2/token" in new TokenExchangeContext {
    post("/oauth2/token").status = Status.Ok

  }

  expectedUserRelatedMothershipEndpoint foreach {
    case (method, endpoint) =>
      s"User-related mothership dispatcher should handle $method at $endpoint" in new UserRelatedMothershipContext {
        method match {
          case Get => get(endpoint).status ==== Status.Ok
          case Post => post(endpoint).status ==== Status.Ok
          case Put => put(endpoint).status ==== Status.Ok
          case Head => head(endpoint).status ==== Status.Ok
          case m => throw new UnsupportedOperationException(s"Test for method $m not implemented")
        }
      }
  }
}
