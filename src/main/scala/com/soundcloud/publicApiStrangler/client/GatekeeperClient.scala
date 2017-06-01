package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.ResponseHandlers.{BooleanByStatusResponse, StringSetResponse}
import com.twitter.util.Future

class GatekeeperClient(service: JsonClient) {
  def featuresFor(session: UserSession): Future[Set[String]] =
    service.getWithSession(
      session,
      Path() / "users" / userIdOrAnonymous(session) / "features",
      Params.empty,
      Headers.empty
    ).map(StringSetResponse(_))

  def isFeatureAccessible(session: UserSession, featureName: String): Future[Boolean] =
    service.head(
      session,
      Path() / "users" / userIdOrAnonymous(session) / "features" / featureName,
      Params.empty,
      Headers.empty,
      None
    ).map(BooleanByStatusResponse(_))

  private def userIdOrAnonymous(session: UserSession): String = {
    if (session.isAnonymous) "anonymous" else session.getUser.getIdentifier
  }
}
