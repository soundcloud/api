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

  "GET /groups/:group_id/users" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/groups/12345/users")
      response.status ==== Status.Ok
    }

    "falls back .json requests to Mothership" in new Context {
      val response = get(controller, "/groups/12345/users.json")
      response.status ==== Status.Ok
    }
  }

  "GET /groups/:group_id/moderators" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/groups/12345/moderators")
      response.status ==== Status.Ok
    }

    "falls back .json requests to Mothership" in new Context {
      val response = get(controller, "/groups/12345/moderators.json")
      response.status ==== Status.Ok
    }
  }

  "GET /groups/:group_id/contributors" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/groups/12345/contributors")
      response.status ==== Status.Ok
    }

    "falls back .json requests to Mothership" in new Context {
      val response = get(controller, "/groups/12345/contributors.json")
      response.status ==== Status.Ok
    }
  }

  "GET /groups/:group_id/members" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/groups/12345/members")
      response.status ==== Status.Ok
    }

    "falls back .json requests to Mothership" in new Context {
      val response = get(controller, "/groups/12345/members.json")
      response.status ==== Status.Ok
    }
  }

  "GET /groups/:group_id/tracks" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/groups/12345/tracks")
      response.status ==== Status.Ok
    }

    "falls back .json requests to Mothership" in new Context {
      val response = get(controller, "/groups/12345/tracks.json")
      response.status ==== Status.Ok
    }
  }
}
