package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.scalakit.Urn
import com.twitter.util.Future

class TracksService {
  def track(urn: Urn): Future[SingleTrackPublicApiRepresentation] = {
    Future.value(new SingleTrackPublicApiRepresentation("track", 1L, 1L))
  }
}
