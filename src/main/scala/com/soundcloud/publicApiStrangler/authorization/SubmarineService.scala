package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Finds active subscriptions and respective content for a particular [[UserSession]]
  *
  * @param submarine A client to [[https://github.com/soundcloud/submarine Submarine]]
  */
class SubmarineService(submarine: JsonClient) {
  def getActiveSubscriptionCountry(session: UserSession): Future[Option[String]] = {
    val path = Path() / "api" / "users" / session.getUser / "consumer_subscriptions" / "active"
    submarine.getWithSession(session, path, Params.empty).map { response =>
      response.status match {
        case Status.Ok => (Json.parse(response.contentString) \ "country_code").asOpt[String]
        case Status.NotFound => None
        case _ => throw UnhandledResponseException(response)
      }
    }
  }
}
