package com.soundcloud.publicApiStrangler.subscriptions

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class SubmarineClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val serviceMock = smartMock[JsonClient]
    val creatorSubscriptionsResponseMapperMock = smartMock[SubmarineCreatorSubscriptionsResponseMapper]
    val subject = new SubmarineClient(
      serviceMock,
      creatorSubscriptionsResponseMapperMock
    )
    val session = anonymousSession
    val response = JsonResponseBuilder.ok("")
  }

  "fetchActiveCreatorSubscriptions" >> {
    trait ActiveCreatorSubscriptionsContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "1"), Urn("soundcloud", "users", "2"))
    }

    "calls the service and maps the response" in new ActiveCreatorSubscriptionsContext {
      val subscriptions = ActiveSubmarineCreatorSubscriptionsResponse(Map.empty)

      when(
        serviceMock.getWithSession(
          session,
          Path() / "api" / "creator_subscriptions" / "bulk" / "active",
          Params("urns" -> urns),
          Headers.empty()
        )
      ).thenReturn(Future(response))

      when(creatorSubscriptionsResponseMapperMock.apply(response)).thenReturn(subscriptions)

      Await.result(subject.fetchActiveCreatorSubscriptions(session, urns)) ==== subscriptions
    }
  }
}
