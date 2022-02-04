package com.soundcloud.apipublic.subscriptions

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.util.Future

class SubmarineClient(
    client: JsonClient
) {

  def fetchActiveCreatorSubscriptions(
      session: UserSession,
      urns: Set[Urn]
  ): Future[Map[Urn, Option[SubmarineCreatorSubscription]]] = {

    client
      .getWithSession(
        session,
        Path() / "api" / "creator_subscriptions" / "bulk" / "active",
        Params("urns" -> urns),
        Headers.empty()
      )
      .map(SubmarineCreatorSubscriptionsResponseMapper(_))
  }
}
