package com.soundcloud.apipublic.client.applications

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Await
import org.mockito.Mockito.when
import play.api.libs.json.Json

class CreatorSubscriptionsClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val submarineClient = mock[JsonClient]
    val client = new CreatorSubscriptionsClient(submarineClient)
    val user = Urn("soundcloud", "users", "2")
    val session = anonymousSession

    def activeSubscriptionResponse: Response = {
      val response = Response(Status.Ok)
      response.setContentString(
        Json.stringify(
          Json.obj(
            "state" -> "active",
            "package" -> Json.obj("plan" -> "pro-unlimited")
          )
        )
      )
      response
    }
  }

  "hasActiveProUnlimited" >> {
    "returns Good(true) for active pro-unlimited subscription" in new Context {
      when(
        submarineClient.getWithSession(
          session,
          Path("/api") / "users" / user / "creator_subscriptions" / "active",
          Params.empty,
          Headers.empty()
        )
      ).thenReturn(com.twitter.util.Future.value(activeSubscriptionResponse))

      Await.result(client.hasActiveProUnlimited(session, user)) ==== Good(true)
    }

    "returns Good(false) when subscription is not found" in new Context {
      when(
        submarineClient.getWithSession(
          session,
          Path("/api") / "users" / user / "creator_subscriptions" / "active",
          Params.empty,
          Headers.empty()
        )
      ).thenReturn(com.twitter.util.Future.value(Response(Status.NotFound)))

      Await.result(client.hasActiveProUnlimited(session, user)) ==== Good(false)
    }

    "returns Bad when response body is invalid JSON" in new Context {
      val response = Response(Status.Ok)
      response.setContentString("not json")
      when(
        submarineClient.getWithSession(
          session,
          Path("/api") / "users" / user / "creator_subscriptions" / "active",
          Params.empty,
          Headers.empty()
        )
      ).thenReturn(com.twitter.util.Future.value(response))

      Await.result(client.hasActiveProUnlimited(session, user)) must beAnInstanceOf[Bad]
    }

    "returns Bad when upstream fails" in new Context {
      when(
        submarineClient.getWithSession(
          session,
          Path("/api") / "users" / user / "creator_subscriptions" / "active",
          Params.empty,
          Headers.empty()
        )
      ).thenReturn(com.twitter.util.Future.value(Response(Status.BadGateway)))

      Await.result(client.hasActiveProUnlimited(session, user)) must beAnInstanceOf[Bad]
    }
  }
}
