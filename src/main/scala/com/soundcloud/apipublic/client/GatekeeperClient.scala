package com.soundcloud.apipublic.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.support.ResponseHandlers.StringSetResponse
import com.twitter.util.Future

class GatekeeperClient(service: JsonClient) {
  def featuresFor(session: UserSession): Future[Set[String]] =
    service
      .getWithSession(
        session,
        Path() / "users" / userIdOrAnonymous(session) / "features",
        Params.empty,
        Headers.empty
      )
      .map(StringSetResponse(_))

  def isFeatureAccessible(session: UserSession, featureName: String): Future[Boolean] =
    service
      .getWithSession(
        session,
        Path() / "users" / userUrnOrAnonymous(session) / "features",
        Params.empty,
        Headers.empty
      )
      .map(StringSetResponse(_).contains(featureName))

  private def userIdOrAnonymous(session: UserSession): String = {
    if (session.isAnonymous) "anonymous" else session.getUser.identifier
  }

  private def userUrnOrAnonymous(session: UserSession): String = {
    if (session.isAnonymous) "anonymous" else Option(session.getUser).map(_.toString).getOrElse("anonymous")
  }
}
