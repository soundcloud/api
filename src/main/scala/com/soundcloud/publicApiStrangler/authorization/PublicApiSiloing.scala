package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future

class PublicApiSiloing(checkRollout: () => Future[Boolean], blacklistOfAppIDs: Set[Urn], telemetry: Telemetry) {
  private lazy val apiSiloingCounter =
    telemetry.counter("app_siloed_requests", "Requests going through app siloing", "result")

  /**
    * SoundCloud-internal applications such as the web client ("v2") and the mobile clients
    * no longer use public api endpoints to do work, at least for most purposes.
    *
    * The siloing logic below ensures that api keys illegitimately extracted by
    * third parties from SoundCloud-internal applications are not being used to call
    * into the public api (to, for example, download tracks en masse).
    * "Silo" means: "SoundCloud-internal apps should use
    * the other BFFs, and should not be hitting the public api, for most purposes".
    */
  def withSiloedSession[T: ResponseLike](userSession: UserSession)(action: => Future[T]): Future[T] = {
    checkRollout().flatMap { rolloutEnabled =>
      val isBlacklisted = blacklisted(userSession)

      if (isBlacklisted)
        apiSiloingCounter.labels("denied").inc()
      else
        apiSiloingCounter.labels("passed").inc()

      if (isBlacklisted && rolloutEnabled)
        Future.value(ResponseLike[T].unauthorized)
      else
        action
    }
  }

  private def blacklisted(userSession: UserSession): Boolean = blacklistOfAppIDs.contains(userSession.getAgent)
}
