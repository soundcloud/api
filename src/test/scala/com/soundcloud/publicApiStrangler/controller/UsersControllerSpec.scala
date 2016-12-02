package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.service.client.LieblingClient
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.mockito.Mockito.when

class UsersControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val userAuthentication = fakeUserAuthentication(anonymousSession)
    val mothershipDispatcher = mock[DispatchToMothershipHandler]
    val followCountsClient = mock[FollowCountsClient]
    val lieblingClient = mock[LieblingClient]
    val controller = new UsersController(userAuthentication, mothershipDispatcher, followCountsClient, lieblingClient)

    val success = Future.value(new ResponseBuilder().status(200).build)
    when(mothershipDispatcher.defaultHandling(any)).thenReturn(success)
  }

  "GET /users/:id" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/users/7110")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/users/7110/")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/users/7110.json")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json and trailing slash" in new Context {
      val response = get(controller, "/users/7110.json/")
      response.status ==== Status.Ok
    }
  }

  "GET /users/me" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/users/me")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/users/me/")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/users/me.json")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json and trailing slash" in new Context {
      val response = get(controller, "/users/me.json/")
      response.status ==== Status.Ok
    }
  }

  "GET /users/:id/tracks" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/users/7110/tracks")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/users/7110/tracks/")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/users/7110/tracks.json")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json and trailing slash" in new Context {
      val response = get(controller, "/users/7110/tracks.json/")
      response.status ==== Status.Ok
    }
  }

  "GET /users/:id/comments" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/users/7110/comments")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/users/7110/comments/")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/users/7110/comments.json")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json and trailing slash" in new Context {
      val response = get(controller, "/users/7110/comments.json/")
      response.status ==== Status.Ok
    }
  }
}
