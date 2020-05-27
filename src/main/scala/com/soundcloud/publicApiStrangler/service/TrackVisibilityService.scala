package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, MonetizationModel}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, TracksClient, VisibleTrack}
import com.twitter.util.Future

class TrackVisibilityService(tracksClient: TracksClient, whitelistedClients: Set[Urn]) {

  def tracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[VisibleTrack]] = {
    for {
      visibleTracks <- tracksClient.visibleTracks(session, trackRequests)
      filteredVisibleTracks = visibleTracks.filter { track =>
        track.disabledAt.isEmpty && // Filters tracks that are disabled (taken down or over quota)
        track.transcodings.exists(_.mimeType == "audio/mpeg") && // Filters out non playable tracks (missing transcoding)
        track.authorization.policy != ContentPolicy.BLOCK &&
        (whitelistedClients.contains(session.getAgent) || !isPaywalledTrack(track)) // Filters out paywalled tracks unless client is whitelisted
      }
    } yield {
      filteredVisibleTracks
    }
  }

  private def isPaywalledTrack(track: VisibleTrack): Boolean = {
    track.authorization.policy == ContentPolicy.MONETIZE &&
    track.authorization.getMonetizationModel == MonetizationModel.SUB_HIGH_TIER
  }
}
