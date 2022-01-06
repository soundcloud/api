package com.soundcloud.apipublic.subscriptions

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.{Response, Status}
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

class SubmarineCreatorSubscriptionsResponseMapperSpec extends Specification with Mockito {
  trait Context extends Scope {
    val creatorSubscriptionsMapper = smartMock[SubmarineCreatorSubscriptionsMapper]
    val subject = new SubmarineCreatorSubscriptionsResponseMapper(creatorSubscriptionsMapper)

    def status: Status

    def body: String

    def creatorSubscriptionResponse: Response = JsonResponseBuilder(status, body).build
  }

  "when response is OK status" >> {
    trait OkContext extends Context {
      val activeCreatorSubscription = Map.empty[Urn, Option[SubmarineCreatorSubscription]]
      val someJsValue = Json.obj("some" -> "jsValue")

      override def status = Status.Ok

      override def body = """{"some":"jsValue"}"""

      when(creatorSubscriptionsMapper.apply(someJsValue)).thenReturn(activeCreatorSubscription)
    }

    "returns an ActiveCreatorSubscriptionsResponse" in new OkContext {
      subject.apply(creatorSubscriptionResponse) ==== ActiveSubmarineCreatorSubscriptionsResponse(
        activeCreatorSubscription
      )
    }
  }

  "when response is NOT FOUND status" >> {
    trait NotFoundContext extends Context {
      override def status = Status.NotFound

      override def body = ""
    }

    "returns a NoCreatorSubscriptionsResponse" in new NotFoundContext {
      subject.apply(creatorSubscriptionResponse) ==== NoSubmarineCreatorSubscriptionsResponse
    }
  }
}
