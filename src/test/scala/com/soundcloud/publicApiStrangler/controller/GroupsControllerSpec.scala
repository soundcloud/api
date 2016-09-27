package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.finagle.http.Status
import com.twitter.util.Future


class GroupsControllerSpec extends InjectionBasedControllerSpecification {
  trait Context extends Scope with VerifiedMocks {
    val userAuthentication = fakeUserAuthentication(anonymousSession)
    val mothershipDispatcher = mock[DispatchToMothershipHandler]
    val followCountsClient = mock[FollowCountsClient]
    val controller = new GroupsController(userAuthentication, mothershipDispatcher, followCountsClient)

    val success = Future.value(new ResponseBuilder().status(200).build)
    when(mothershipDispatcher.defaultHandling(any)).thenReturn(success)
  }

  "GET /groups" >> {
    "returns empty list" in new Context {
      val response = get(controller, "/groups")
      response.status ==== Status.Ok
      response.body ==== "[]"
    }

    "returns empty list" in new Context {
      val response = get(controller, "/groups.json")
      response.status ==== Status.Ok
      response.body ==== "[]"
    }
  }

  "GET /me/groups" >> {
    "returns empty list" in new Context {
      val response = get(controller, "/me/groups")
      response.status ==== Status.Ok
      response.body ==== "[]"
    }

    "returns empty list" in new Context {
      val response = get(controller, "/me/groups.json")
      response.status ==== Status.Ok
      response.body ==== "[]"
    }
  }

  "GET /groups/:group_id" >> {
    "returns not found" in new Context {
      val response = get(controller, "/groups/12345")
      response.status ==== Status.NotFound
    }

    "returns not found" in new Context {
      val response = get(controller, "/groups/12345.json")
      response.status ==== Status.NotFound
    }
  }

  "GET /groups/:group_id/users" >> {
    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/users")
      response.status ==== Status.NotFound
    }

    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/users.json")
      response.status ==== Status.NotFound
    }
  }

  "GET /groups/:group_id/moderators" >> {
    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/moderators")
      response.status ==== Status.NotFound
    }

    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/moderators.json")
      response.status ==== Status.NotFound
    }
  }

  "GET /groups/:group_id/contributors" >> {
    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/contributors")
      response.status ==== Status.NotFound
    }

    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/contributors.json")
      response.status ==== Status.NotFound
    }
  }

  "GET /groups/:group_id/members" >> {
    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/members")
      response.status ==== Status.NotFound
    }

    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/members.json")
      response.status ==== Status.NotFound
    }
  }

  "GET /groups/:group_id/tracks" >> {
    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/tracks")
      response.status ==== Status.NotFound
    }

    "returns not found" in new Context {
      val response = get(controller, "/groups/12345/tracks.json")
      response.status ==== Status.NotFound
    }
  }
}
