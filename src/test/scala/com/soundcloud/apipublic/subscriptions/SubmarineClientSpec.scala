package com.soundcloud.apipublic.subscriptions

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.Json

class SubmarineClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val serviceMock = smartMock[JsonClient]
    val subject = new SubmarineClient(
      serviceMock
    )
    val session = anonymousSession
    val response = JsonResponseBuilder.ok(Json.stringify(Fixtures.submarineCreatorSubscription))
  }

  "fetchActiveCreatorSubscriptions" >> {
    trait ActiveCreatorSubscriptionsContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "1"), Urn("soundcloud", "users", "2"))
    }

    "calls the service and maps the response" in new ActiveCreatorSubscriptionsContext {
      when(
        serviceMock.getWithSession(
          session,
          Path() / "api" / "creator_subscriptions" / "bulk" / "active",
          Params("urns" -> urns),
          Headers.empty()
        )
      ).thenReturn(Future(response))

      val expectedResult =
        Map(
          Urn("soundcloud", "users", "123") -> Some(
            SubmarineCreatorSubscription(false, Package("Yearly Pro plan", "pro"))
          ),
          Urn("soundcloud", "users", "2") -> None
        )

      Await.result(subject.fetchActiveCreatorSubscriptions(session, urns)) ==== expectedResult
    }
  }
}
