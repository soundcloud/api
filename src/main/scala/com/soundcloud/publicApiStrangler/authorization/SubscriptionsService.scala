package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

class SubscriptionsService(subscriptions: JsonClient) {
  def getActiveSubscriptionCountry(session: UserSession): Future[String] = {
    val path = Path() / "api" / "users" / session.getUser / "consumer_subscriptions" / "active"
    subscriptions.getWithSession(session, path, Params.empty).map { response =>
      response.status match {
        case Status.Ok => (Json.parse(response.contentString) \ "country_code").as[String]
        // This function only ever gets called if a user is known to have a consumer subscription, and their
        // subscription country is required for authsy or search, so it is an error if we cannot retrieve it.
        case _ => throw new UnhandledResponseException(response)
      }
    }
  }
}
