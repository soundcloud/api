package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsNull

class SubscriptionsServiceSpec extends UnitSpecification {

  "#getActiveSubscriptionCountry" >> {
    trait Context extends Scope {
      val client = mock[JsonClient]
      val service = new SubscriptionsService(client)
      val user = Urn("soundcloud:users:66")
      val session = loggedInSession(user)
      val activeConsumerSubPath = Path("/api") / "users" / user / "consumer_subscriptions" / "active"
    }

    "returns country code when subscription exists" in new Context {
      val response = jsonResponse(Status.Ok, consumerSubscription)
      client.getWithSession(===(session), ===(activeConsumerSubPath), any[Params], any[Headers]) returns Future.value(response)

      Await.result(service.getActiveSubscriptionCountry(session)) ==== Some("US")
    }

    "returns nothing when subscription does not exist" in new Context {
      val response = jsonResponse(Status.NotFound, consumerSubscription)
      client.getWithSession(===(session), ===(activeConsumerSubPath), any[Params], any[Headers]) returns Future.value(response)

      Await.result(service.getActiveSubscriptionCountry(session)) ==== None
    }

    "throws exception when subscription cannot be retrieved" in new Context {
      val response = jsonResponse(Status.InternalServerError, JsNull)
      client.getWithSession(===(session), ===(activeConsumerSubPath), any[Params], any[Headers]) returns Future.value(response)

      Await.result(service.getActiveSubscriptionCountry(session)) must throwAn[UnhandledResponseException]
    }
  }
}
