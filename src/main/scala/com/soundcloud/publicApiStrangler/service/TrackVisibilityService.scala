package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, MonetizationModel}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.service.tracks.VisibleTrackMapper
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{
  GetVisibleTracksRequest,
  Track,
  TrackMetadataService,
  TrackRequest => TwirpTrackRequest
}
import scalapb.FieldMaskUtil

class TrackVisibilityService(
    tracksTwinagleClient: TrackMetadataService,
    visibleTrackMapper: VisibleTrackMapper,
    allowlistedClients: Set[Urn]
) {
  def tracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[VisibleTrack]] = {
    for {
      visibleTracks <- fetchVisibleTracks(session, trackRequests)
      filteredVisibleTracks = {
        visibleTracks.filter { track =>
          track.disabledAt.isEmpty && // Filters tracks that are disabled (taken down or over quota)
          track.transcodings.exists(_.mimeType == "audio/mpeg") && // Filters out non playable tracks (missing transcoding)
          track.authorization.policy != ContentPolicy.BLOCK &&
          (allowlistedClients.contains(session.getAgent) || !isPaywalledTrack(track)) // Filters out paywalled tracks unless client is allowlisted
        }
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
      trackFieldMask = Some(TrackVisibilityService.TrackFieldMask),
      userSession = Some(session.asProtoSession)
    )

    tracksTwinagleClient.getVisibleTracks(request).map { tracksResponse =>
      tracksResponse.tracks.toList
        .map(visibleTrackMapper.apply)
    }
  }
}

object TrackVisibilityService {
  val TrackFieldMask = FieldMaskUtil.selectFieldNumbers[Track](
    Set(
      Track.METADATA_FIELD_NUMBER,
      Track.TRANSCODINGS_FIELD_NUMBER,
      Track.WAVEFORM_URLS_FIELD_NUMBER,
      Track.AUTHORIZATION_FIELD_NUMBER
    )
  )
}
