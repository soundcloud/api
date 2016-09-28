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
    val controller = new GroupsController(userAuthentication)
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

    "returns empty list" in new Context {
      val response = get(controller, "/groups/")
      response.status ==== Status.Ok
      response.body ==== "[]"
    }

    "returns empty list" in new Context {
      val response = get(controller, "/groups.json/")
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

    "returns empty list" in new Context {
      val response = get(controller, "/me/groups/")
      response.status ==== Status.Ok
      response.body ==== "[]"
    }

    "returns empty list" in new Context {
      val response = get(controller, "/me/groups.json/")
      response.status ==== Status.Ok
      response.body ==== "[]"
    }
  }

  "GET /groups/:group_id" >> {
    "returns not found" in new Context {
      List("/groups/123", "/groups/123/", "/groups/123.json", "/groups/123.json/").foreach { path =>
        val response = get(controller, path)
        response.status ==== Status.NotFound
      }
    }
  }

  "GET /groups/:group_id/users" >> {
    "returns not found" in new Context {
      List("/groups/123/users", "/groups/123/users/", "/groups/123/users.json", "/groups/123/users.json/").foreach { path =>
        val response = get(controller, path)
        response.status ==== Status.NotFound
      }
    }
  }

  "GET /groups/:group_id/moderators" >> {
    "returns not found" in new Context {
      List("/groups/123/moderators", "/groups/123/moderators/", "/groups/123/moderators.json", "/groups/123/moderators.json/").foreach { path =>
        val response = get(controller, path)
        response.status ==== Status.NotFound
      }
    }
  }

  "GET /groups/:group_id/contributors" >> {
    "returns not found" in new Context {
      List("/groups/123/contributors", "/groups/123/contributors/", "/groups/123/contributors.json", "/groups/123/contributors.json/").foreach { path =>
        val response = get(controller, path)
        response.status ==== Status.NotFound
      }
    }
  }

  "GET /groups/:group_id/members" >> {
    "returns not found" in new Context {
      List("/groups/123/members", "/groups/123/members/", "/groups/123/members.json", "/groups/123/members.json/").foreach { path =>
        val response = get(controller, path)
        response.status ==== Status.NotFound
      }
    }
  }

  "GET /groups/:group_id/tracks" >> {
    "returns not found" in new Context {
      List("/groups/123/tracks", "/groups/123/tracks/", "/groups/123/tracks.json", "/groups/123/tracks.json/").foreach { path =>
        val response = get(controller, path)
        response.status ==== Status.NotFound
      }
    }
  }
}
