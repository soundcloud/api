package com.soundcloud.publicApiStrangler.service

import com.soundcloud.publicApiStrangler.client.tracks.{TracksClient, VisibleTrack, TrackRequest}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future

class TrackVisibilityService(tracksClient: TracksClient) {

  def tracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[VisibleTrack]] = {
    for {
      visibleTracks <- tracksClient.visibleTracks(session, trackRequests)
      filteredVisibleTracks = visibleTracks.filter { track =>
        // Filters tracks that are disabled and those one that are not playable
        track.disabledAt.isEmpty && track.transcodings.exists(_.mimeType == "audio/mpeg")
      }
    } yield {
      filteredVisibleTracks
    }
  }
}
