package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.finagle.ResponseLike
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.twitter.util.Future
import com.soundcloud.jvmkit.telemetry.Telemetry

class PublicApiSiloing(checkRollout: () => Future[Boolean], blacklistOfAppIDs: Set[Urn], telemetry: Telemetry) {
  private lazy val apiSiloingCounter =
    telemetry.counter("app_siloed_requests", "Requests going through app siloing", "result")

  /**
    * siloes an endpoint by checking if a token was not issued for mobile app if it is the call is rejected
    */
  def withSiloedSession[T: ResponseLike](userSession: UserSession)(action: => Future[T]): Future[T] =  {
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
