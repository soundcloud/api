package com.soundcloud.publicApiStrangler.controller

import java.nio.charset.Charset

import com.soundcloud.scalakit.Urn
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{DefaultHttpHeaders, HttpRequest, HttpResponseStatus}

class GroupControllerSpec extends ControllerSpec {
  "GroupController" should {
    trait ForwardContext extends Scope {
      val forwardStatus = HttpResponseStatus.FOUND
      val forwardContent = "forwardContent"
      val forwardResponse = mock[Response]
      forwardResponse.getStatus returns forwardStatus
      forwardResponse.getContent returns copiedBuffer(forwardContent, Charset.forName("UTF-8"))
      forwardResponse.headers returns new DefaultHttpHeaders
      publicApiClientMock.apply(any[HttpRequest]) returns Future(forwardResponse)
      controller.loggedInAs(Urn("soundcloud:users:123"))

      def stillForwards = {
        response.code ==== forwardStatus.getCode
        response.body ==== forwardContent
      }

      def doesNotForward = {
        response.code ==== 200
        response.body ==== ""
        there was noCallsTo(publicApiClientMock)
      }
    }

    "when disable_cheap_groups_endpoints via Gatekeeper" >> {
      trait Context extends ForwardContext {
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_cheap_groups_endpoints") returns Future.value(true)
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_expensive_groups_endpoints") returns Future.value(false)
      }

      "does not forward /users/:id/groups.json" in new Context {
        get("/users/123/groups.json")
        doesNotForward
      }

      "does not forward /groups/:id.json" in new Context {
        get("/groups/123.json")
        doesNotForward
      }

      "still forwards /groups/:id/users.json" in new Context {
        get("/groups/123/users.json")
        stillForwards
      }

      "still forwards /groups/:id/users" in new Context {
        get("/groups/123/users")
        stillForwards
      }
    }
    "when disable_expensive_groups_endpoints via Gatekeeper" >> {
      trait Context extends ForwardContext {
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_cheap_groups_endpoints") returns Future.value(false)
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_expensive_groups_endpoints") returns Future.value(true)
      }

      "still forwards /users/:id/groups.json" in new Context {
        get("/users/123/groups.json")
        stillForwards
      }

      "still forwards /groups/:id.json" in new Context {
        get("/groups/123.json")
        stillForwards
      }

      "does not forward /groups/:id/users.json" in new Context {
        get("/groups/123/users.json")
        doesNotForward
      }

      "does not forward /groups/:id/users" in new Context {
        get("/groups/123/users")
        doesNotForward
      }
    }

    "when all endpoints accessible" >> {
      trait Context extends ForwardContext {
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_cheap_groups_endpoints") returns Future.value(false)
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_expensive_groups_endpoints") returns Future.value(false)
      }

      "still forwards /users/:id/groups.json" in new Context {
        get("/users/123/groups.json")
        stillForwards
      }

      "still forwards /groups/:id.json" in new Context {
        get("/groups/123.json")
        stillForwards
      }

      "does not forward /groups/:id/users.json" in new Context {
        get("/groups/123/users.json")
        stillForwards

      }

      "does not forward /groups/:id/users" in new Context {
        get("/groups/123/users")
        stillForwards
      }
    }

    "treats Gatekeeper exception as false" >> {
      trait Context extends ForwardContext {
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_cheap_groups_endpoints") returns Future.exception(new RuntimeException("expected"))
        gatekeeperClientMock.isFeatureAccessible(userSession, "disable_expensive_groups_endpoints") returns Future.exception(new RuntimeException("expected"))
      }

      "still forwards /users/:id/groups.json" in new Context {
        get("/users/123/groups.json")
        stillForwards
      }

      "still forwards /groups/:id.json" in new Context {
        get("/groups/123.json")
        stillForwards
      }

      "does not forward /groups/:id/users.json" in new Context {
        get("/groups/123/users.json")
        stillForwards
      }

      "does not forward /groups/:id/users" in new Context {
        get("/groups/123/users")
        stillForwards
      }
    }
  }
}
