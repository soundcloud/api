package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, MonetizationModel}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.service.tracks.VisibleTrackMapper
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{GetVisibleTracksRequest, TracksService, TrackRequest => TwirpTrackRequest}

class TrackVisibilityService(
    tracksTwinagleClient: TracksService,
    visibleTrackMapper: VisibleTrackMapper,
    whitelistedClients: Set[Urn]
) {
  def tracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[VisibleTrack]] = {
    for {
      visibleTracks <- fetchVisibleTracks(session, trackRequests)
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

  private def fetchVisibleTracks(
      session: UserSession,
      trackRequests: Seq[TrackRequest]
  ): Future[List[VisibleTrack]] = {
    val request = GetVisibleTracksRequest(
      trackRequests = trackRequests.toList.map(trackRequest =>
        TwirpTrackRequest(trackRequest.urn.toString, trackRequest.secretToken)
      ),
      userSession = Some(session.asProtoSession)
    )

    tracksTwinagleClient.getVisibleTracks(request).map { tracksResponse =>
      tracksResponse.tracks.toList
        .map(visibleTrackMapper.apply)
    }
  }
}
