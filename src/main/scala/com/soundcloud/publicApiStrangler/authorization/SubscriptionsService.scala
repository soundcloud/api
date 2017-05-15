package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.Params
import com.soundcloud.service.response.mapper.UnhandledResponseException
import com.twitter.util.Future

class SubscriptionsService(subscriptions: JsonService) {
  def getActiveSubscriptionCountry(session: UserSession): Future[String] = {
    val path = Path() / "api" / "users" / session.getUser / "consumer_subscriptions" / "active"
    subscriptions.get(session, path, Params.empty).map { response =>
      response.status match {
        case OkStatus => (response.body \ "country_code").as[String]
        // This function only ever gets called if a user is known to have a consumer subscription, and their
        // subscription country is required for authsy or search, so it is an error if we cannot retrieve it.
        case _ => throw new UnhandledResponseException(response)
      }
    }
  }
}
