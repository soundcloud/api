package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.outcome.Outcome
import com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper._
import com.twitter.util.Future

class TrackCoordinatorClient(service: JsonClient) {
  def deleteTrack(session: UserSession, trackUrn: Urn): Future[Outcome[Unit]] = {
    service
      .deleteWithSession(session, Path("/tracks") / trackUrn, Params.empty, Headers.empty(), None)
      .map(TrackDeleteResponseMapper(_))
  }
}
