package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.outcome.{CustomError, Outcome, _}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.AllowlistedClients
import com.soundcloud.publicApiStrangler.authorization.policies.{Access, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
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
    visibleTrackMapper: VisibleTrackMapper
) {
  def visibleTracks(
      session: UserSession,
      trackRequests: List[TrackRequest],
      access: AccessParams
  ): Future[List[VisibleTrack]] = {
    tracks(session, trackRequests, access).map(allTracks => allTracks.filter(_.isRight).map(_.right.get))
  }

  def tracks(
      session: UserSession,
      trackRequests: List[TrackRequest],
      access: AccessParams
  ): Future[List[Outcome[VisibleTrack]]] = {
    fetchVisibleTracks(session, trackRequests, access)
  }

  private def fetchVisibleTracks(
      session: UserSession,
      trackRequests: Seq[TrackRequest],
      access: AccessParams
  ): Future[List[Outcome[VisibleTrack]]] = {

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
        .filter(visibleTrack => visibleTrack.disabledAt.isEmpty)
        .map(applyRules(session.getAgent, _, access.access))
    }
  }

  private def applyRules(client: Urn, track: VisibleTrack, allowedAccesses: Set[Access]): Outcome[VisibleTrack] = {
    val trackAccess =
      getAccessFromPolicy(
        track.authorization.policy,
        track.authorization.monetizationModel,
        track.apiStreamable.getOrElse(true),
        client
      )

    if (allowedAccesses.contains(trackAccess)) {
      track.copy(access = Some(trackAccess)).good
    } else if (!track.apiStreamable.getOrElse(true)) {
      CustomError(UnavailableByPolicy(track.urn, Reason.NOT_SUPPORTED)).bad
    } else {
      CustomError(UnavailableByPolicy(track.urn, track.authorization.reason)).bad
    }
  }

  private def getAccessFromPolicy(
      policy: ContentPolicy,
      model: MonetizationModel,
      apiStreamable: Boolean,
      client: Urn
  ): Access = {
    (policy, model, apiStreamable) match {
      case (_, _, false) | (ContentPolicy.BLOCK, _, _) => Access.Blocked
      case (ContentPolicy.MONETIZE, MonetizationModel.SUB_HIGH_TIER, _) =>
        if (!AllowlistedClients.clients.contains(client)) Access.Blocked else Access.Preview
      case (ContentPolicy.SNIP, _, _) => Access.Preview
      case _ => Access.Playable
    }
  }
}

object TrackVisibilityService {
  val TrackFieldMask = FieldMaskUtil.selectFieldNumbers[Track](
    Set(
      Track.METADATA_FIELD_NUMBER,
      Track.TRANSCODINGS_FIELD_NUMBER,
      Track.WAVEFORM_URLS_FIELD_NUMBER,
      Track.AUTHORIZATION_FIELD_NUMBER,
      Track.COUNTS_FIELD_NUMBER,
      Track.DOWNLOAD_METADATA_FIELD_NUMBER,
      Track.PUBLISHER_METADATA_FIELD_NUMBER
    )
  )
}
