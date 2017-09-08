package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.{Method, Request, Status}
import com.twitter.util.Future

class DispatchingToMotherShipSpec extends UnitSpecification {

  val expectedMotherShipEndpoints = Set(
    (Get, "/announcements"),
    (Get, "/announcements.json"),
    (Post, "/oauth2/token"),
    (Post, "/oauth2/token/"),
    (Post, "/oauth2/token.json"),
    (Post, "/playlists"),
    (Put, "/playlists/1"),
    (Put, "/playlists/1.json"),
    (Get, "/resolve"),
    (Get, "/resolve.json"),
    (Get, "/tracks/999/comments"),
    (Get, "/tracks/999/comments/"),
    (Get, "/tracks/999/comments.json"),
    (Get, "/tracks/999/comments.json/"),
    (Get, "/tracks/999/download"),
    (Get, "/tracks/999/download/"),
    (Get, "/tracks/999/download.json"),
    (Get, "/tracks/999/download.json/"),
    (Post, "/tracks/999"),
    (Post, "/tracks/999.json"),
    (Post, "/users/7110/tracks")
  )

  val expectedUserRelatedMothershipEndpoint = Set(
    (Get, "/users/7110"),
    (Get, "/users/7110/"),
    (Get, "/users/7110.json"),
    (Get, "/users/7110.json/"),
    (Get, "/users/7110/comments"),
    (Get, "/users/7110/comments/"),
    (Get, "/users/7110/comments.json"),
    (Get, "/users/7110/comments.json/"),
    (Get, "/users/me"),
    (Get, "/users/me/"),
    (Get, "/users/me.json"),
    (Get, "/users/me.json/"),
    (Get, "/me/suggested/users/somecategory"),
    (Get, "/me/suggested/users/:somecategory.json"),
    (Get, "/users/suggested"),
    (Get, "/users/suggested.json"),
    (Get, "/me/connections/friends"),
    (Get, "/me/connections/friends.json"),
    (Get, "/tracks/7110/favoriters"),
    (Get, "/tracks/7110/favoriters.json"),
    (Get, "/tracks/7110/favoriters/123"),
    (Get, "/tracks/7110/favoriters/123.json"),
    (Get, "/me"),
    (Get, "/me/"),
    (Get, "/me.json"),
    (Get, "/me.json/")
  )

  trait MothershipContext extends HandlerSpecificationScope {
    val dispatcher = mock[DispatchToMothershipHandler]
    dispatcher.dispatch(any[Request]) returns Future.value(ResponseBuilder.ok())
    dispatcher.dispatchToMothership(any[HandlerRequest]) returns Future.value(ResponseBuilder.ok())

    override def routingDefinitions() = Routing.forMothershipDispatcher(dispatcher)
  }

  trait UserRelatedMothershipContext extends HandlerSpecificationScope {
    val dispatcher = mock[UserRelatedMothershipDispatcher]
    dispatcher.dispatchToMothership(any[HandlerRequest]) returns Future.value(ResponseBuilder.ok())

    override def routingDefinitions() = Routing.forUserRelatedMothershipDispatcher(dispatcher)
  }


  expectedMotherShipEndpoints foreach { case (method, endpoint) =>
    s"Mothership dispatcher should handle $method at $endpoint" in new MothershipContext {
      method match {
        case Method.Get => get(dispatcher.dispatchToMothership, endpoint).status = Status.Ok
        case Method.Post => post(dispatcher.dispatchToMothership, endpoint).status = Status.Ok
        case Method.Put => put(dispatcher.dispatchToMothership, endpoint).status = Status.Ok
        case m => throw new UnsupportedOperationException(s"Test for method $m not implemented")
      }
    }
  }

  expectedUserRelatedMothershipEndpoint foreach { case (method, endpoint) =>
    s"User-related mothership dispatcher should handle $method at $endpoint" in new UserRelatedMothershipContext {
      method match {
        case Get => get(dispatcher.dispatchToMothership, endpoint).status ==== Status.Ok
        case Post => post(dispatcher.dispatchToMothership, endpoint).status ==== Status.Ok
        case Put => put(dispatcher.dispatchToMothership, endpoint).status ==== Status.Ok
        case Head => head(dispatcher.dispatchToMothership, endpoint).status ==== Status.Ok
        case m => throw new UnsupportedOperationException(s"Test for method $m not implemented")
      }
    }
  }

}
