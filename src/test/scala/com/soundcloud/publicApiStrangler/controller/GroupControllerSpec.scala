package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.client.GatekeeperClient
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus

class GroupControllerSpec extends InjectionBasedControllerSpecification {

  trait ForwardContext extends Scope {
    val session = loggedInSession(Urn("soundcloud:users:123"))

    val gatekeeperClientMock = mock[GatekeeperClient]
    val forwardHandlerMock = mock[ForwardRequestHandler]

    val controller = new GroupController(fakeUserAuthentication(session), gatekeeperClientMock, forwardHandlerMock)

    val forwardStatus = HttpResponseStatus.FOUND.getCode
    val forwardContent = "forwardContent"

    def expectForwardedRequest =
      forwardHandlerMock.handle(any[Request])
        .returns(Future((new ResponseBuilder()).body(forwardContent).status(forwardStatus)))

    def stillForwards(response: MockResponse) = {
      response.code ==== 302
      response.body ==== "forwardContent"
    }

    def doesNotForward(response: MockResponse) = {
      response.code ==== 200
      response.body ==== ""
      there was noCallsTo(forwardHandlerMock)
    }
  }

  "when disable_cheap_groups_endpoints via Gatekeeper" >> {
    trait Context extends ForwardContext {
      gatekeeperClientMock.isFeatureAccessible(session, "disable_cheap_groups_endpoints") returns Future.value(true)
      gatekeeperClientMock.isFeatureAccessible(session, "disable_expensive_groups_endpoints") returns Future.value(false)
    }

    "does not forward /users/:id/groups.json" in new Context {
      val response = get(controller, "/users/123/groups.json")
      doesNotForward(response)
    }

    "does not forward /groups/:id.json" in new Context {
      val response = get(controller, "/groups/123.json")
      doesNotForward(response)
    }

    "still forwards /groups/:id/users.json" in new Context {
      expectForwardedRequest

      val response = get(controller, "/groups/123/users.json")
      stillForwards(response)
    }

    "still forwards /groups/:id/users" in new Context {
      expectForwardedRequest

      val response = get(controller, "/groups/123/users")
      stillForwards(response)
    }
  }
  "when disable_expensive_groups_endpoints via Gatekeeper" >> {
    trait Context extends ForwardContext {
      gatekeeperClientMock.isFeatureAccessible(session, "disable_cheap_groups_endpoints") returns Future.value(false)
      gatekeeperClientMock.isFeatureAccessible(session, "disable_expensive_groups_endpoints") returns Future.value(true)
    }

    "still forwards /users/:id/groups.json" in new Context {
      expectForwardedRequest

      val response = get(controller, "/users/123/groups.json")
      stillForwards(response)
    }

    "still forwards /groups/:id.json" in new Context {
      expectForwardedRequest

      val response = get(controller, "/groups/123.json")
      stillForwards(response)
    }

    "does not forward /groups/:id/users.json" in new Context {
      val response = get(controller, "/groups/123/users.json")
      doesNotForward(response)
    }

    "does not forward /groups/:id/users" in new Context {
      val response = get(controller, "/groups/123/users")
      doesNotForward(response)
    }
  }

  "when all endpoints accessible" >> {
    trait Context extends ForwardContext {
      gatekeeperClientMock.isFeatureAccessible(session, "disable_cheap_groups_endpoints") returns Future.value(false)
      gatekeeperClientMock.isFeatureAccessible(session, "disable_expensive_groups_endpoints") returns Future.value(false)
      expectForwardedRequest
    }

    "still forwards /users/:id/groups.json" in new Context {
      val response = get(controller, "/users/123/groups.json")
      stillForwards(response)
    }

    "still forwards /groups/:id.json" in new Context {
      val response = get(controller, "/groups/123.json")
      stillForwards(response)
    }

    "does not forward /groups/:id/users.json" in new Context {
      val response = get(controller, "/groups/123/users.json")
      stillForwards(response)
    }

    "does not forward /groups/:id/users" in new Context {
      val response = get(controller, "/groups/123/users")
      stillForwards(response)
    }
  }

  "treats Gatekeeper exception as false" >> {
    trait Context extends ForwardContext {
      gatekeeperClientMock.isFeatureAccessible(session, "disable_cheap_groups_endpoints") returns Future.exception(new RuntimeException("expected"))
      gatekeeperClientMock.isFeatureAccessible(session, "disable_expensive_groups_endpoints") returns Future.exception(new RuntimeException("expected"))
      expectForwardedRequest
    }

    "still forwards /users/:id/groups.json" in new Context {
      val response = get(controller, "/users/123/groups.json")
      stillForwards(response)
    }

    "still forwards /groups/:id.json" in new Context {
      val response = get(controller, "/groups/123.json")
      stillForwards(response)
    }

    "does not forward /groups/:id/users.json" in new Context {
      val response = get(controller, "/groups/123/users.json")
      stillForwards(response)
    }

    "does not forward /groups/:id/users" in new Context {
      val response = get(controller, "/groups/123/users")
      stillForwards(response)
    }
  }
}
