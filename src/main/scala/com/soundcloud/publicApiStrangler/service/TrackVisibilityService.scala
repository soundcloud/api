package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.outcome.{CustomError, Outcome, _}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, MonetizationModel, Reason}
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

case class UnavailableByPolicy(urn: Urn, reason: Reason)

class TrackVisibilityService(
    tracksTwinagleClient: TrackMetadataService,
    visibleTrackMapper: VisibleTrackMapper,
    allowlistedClients: Set[Urn]
) {
  def visibleTracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[VisibleTrack]] = {
    tracks(session, trackRequests).map(allTracks => allTracks.filter(_.isRight).map(_.right.get))
  }

  def tracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[Outcome[VisibleTrack]]] = {
    for {
      visibleTracks <- fetchVisibleTracks(session, trackRequests)
    } yield visibleTracks.map(track => filterAllowed(session.getAgent, track))
  }

  private def filterAllowed(client: Urn, track: VisibleTrack): Outcome[VisibleTrack] = {
    if (track.disabledAt.isEmpty && // Filters tracks that are disabled (taken down or over quota)
      track.transcodings.exists(_.mimeType == "audio/mpeg") && // Filters out non playable tracks (missing transcoding)
      track.authorization.policy != ContentPolicy.BLOCK &&
      isFreeOrAllowlisted(client, track)) // Filters out paywalled tracks unless client is allowlisted
      track.good
    else if (!track.apiStreamable.getOrElse(true) || !isFreeOrAllowlisted(client, track))
      CustomError(UnavailableByPolicy(track.urn, Reason.NOT_SUPPORTED)).bad
    else CustomError(UnavailableByPolicy(track.urn, track.authorization.reason)).bad
  }

  private def isFreeOrAllowlisted(client: Urn, track: VisibleTrack): Boolean = {
    val isPaywalled = track.authorization.policy == ContentPolicy.MONETIZE &&
      track.authorization.getMonetizationModel == MonetizationModel.SUB_HIGH_TIER
    allowlistedClients.contains(client) || !isPaywalled
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
