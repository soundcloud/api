package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.{JsonService, _}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice._
import com.soundcloud.scalakit.{Path, Urn}
import com.soundcloud.service.response.mapper.UnhandledResponseException
import com.twitter.util.{Await, Future}

class SubscriptionsServiceSpec extends UnitSpecification with Fixtures {

  "#getActiveSubscriptionCountry" >> {
    trait Context extends Scope {
      val client = mock[JsonService]
      val service = new SubscriptionsService(client)
      val user = Urn("soundcloud:users:66")
      val session = loggedInSession(user)
      val activeConsumerSubPath = Path("/api") / "users" / user / "consumer_subscriptions" / "active"
    }

    "returns country code when subscription exists" in new Context {
      val response = JsonResponse(OkStatus, consumerSubscription)
      client.get(===(session), ===(activeConsumerSubPath), any[Params], any[Params]) returns Future.value(response)

      Await.result(service.getActiveSubscriptionCountry(session)) ==== "US"
    }

    "throws exception when subscription cannot be retrieved" in new Context {
      val response = JsonResponse(InternalServerErrorStatus, JsNull)
      client.get(===(session), ===(activeConsumerSubPath), any[Params], any[Params]) returns Future.value(response)

      Await.result(service.getActiveSubscriptionCountry(session)) must throwAn[UnhandledResponseException]
    }
  }
}
