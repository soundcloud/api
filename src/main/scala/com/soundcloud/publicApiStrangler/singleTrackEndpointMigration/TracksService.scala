package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.TrackmetadataClient
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.util.Future


class TracksService(trackmetadataClient: TrackmetadataClient) {
  def track(session: UserSession, urn: Urn): Future[Option[SingleTrackPublicApiRepresentation]] = {

    trackmetadataClient.track(session, urn, None).map(_.map {
      case track =>
        new SingleTrackPublicApiRepresentation(
          "track",
          track.urn.getIdentifier.toLong,
          track.user_urn.getIdentifier.toLong)
    })
  }
}
