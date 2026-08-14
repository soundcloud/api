package com.soundcloud.apipublic.service

import com.google.protobuf.field_mask.FieldMask
import com.soundcloud.jvmkit.module.outcome.{CustomError, Outcome, _}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.authorization.AllowlistedClients
import com.soundcloud.apipublic.authorization.policies.{Access, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.apipublic.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService.SUPPLY_CHAIN_STATUS_SUPPLY_CHAIN
import com.soundcloud.apipublic.service.tracks.VisibleTrackMapper
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{
  GetVisibleTracksRequest,
  Track,
  TrackMetadataService,
  TranscodingFilterStrategy,
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
      fieldMask: FieldMask,
      access: AccessParams
  ): Future[List[VisibleTrack]] = {
    tracks(session, trackRequests, fieldMask, access).map(allTracks => allTracks.filter(_.isRight).map(_.right.get))
  }

  def tracks(
      session: UserSession,
      trackRequests: List[TrackRequest],
      fieldMask: FieldMask,
      access: AccessParams
  ): Future[List[Outcome[VisibleTrack]]] = {
    fetchVisibleTracks(session, trackRequests, fieldMask, access)
  }

  private def fetchVisibleTracks(
      session: UserSession,
      trackRequests: Seq[TrackRequest],
      fieldMask: FieldMask,
      access: AccessParams
  ): Future[List[Outcome[VisibleTrack]]] = {

    val request = GetVisibleTracksRequest(
      trackRequests = trackRequests.toList.map(trackRequest =>
        TwirpTrackRequest(trackRequest.urn.toString, trackRequest.secretToken)
      ),
      trackFieldMask = Some(fieldMask),
      userSession = Some(session.asProtoSession),
      transcodingFilterStrategy = transcodingFilterStrategyFor(fieldMask)
    )

    tracksTwinagleClient.getVisibleTracks(request).map { tracksResponse =>
      tracksResponse.tracks.toList
        .map { track =>
          visibleTrackMapper.apply(track, session)
        }
        .filter(visibleTrack => visibleTrack.disabledAt.isEmpty)
        .map(applyRules(session.getAgent, _, access.access))
    }
  }

  private def transcodingFilterStrategyFor(fieldMask: FieldMask): Option[TranscodingFilterStrategy] = {
    if (fieldMask.paths.exists(TrackVisibilityService.TranscodingPaths.contains))
      Some(TranscodingFilterStrategy.LEGACY_AND_NEW)
    else
      None
  }

  private def applyRules(client: Urn, track: VisibleTrack, allowedAccesses: Set[Access]): Outcome[VisibleTrack] = {
    val trackAccess =
      getAccessFromPolicy(
        track.authorization.policy,
        track.authorization.monetizationModel,
        track.apiStreamable.getOrElse(true),
        track.supplyChainStatus,
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
      supplyChainStatus: Option[String],
      client: Urn
  ): Access = {
    (policy, model, apiStreamable, supplyChainStatus) match {
      case (_, _, false, _) | (ContentPolicy.BLOCK, _, _, _) => Access.Blocked
      case (ContentPolicy.MONETIZE, MonetizationModel.SUB_HIGH_TIER, _, _) =>
        if (!AllowlistedClients.clients.contains(client)) Access.Blocked else Access.Preview
      case (ContentPolicy.SNIP, _, _, _) => Access.Preview
      case (_, _, _, Some(SUPPLY_CHAIN_STATUS_SUPPLY_CHAIN)) => Access.Preview
      case _ => Access.Playable
    }
  }
}

object TrackVisibilityService {

  /** Tracks metadata `supply_chain_status` value denoting partner / supply-chain catalogue delivery. */
  final val SUPPLY_CHAIN_STATUS_SUPPLY_CHAIN = "supply_chain"

  val DefaultTrackFieldMask: FieldMask = FieldMaskUtil.selectFieldNumbers[Track](
    Set(
      Track.METADATA_FIELD_NUMBER,
      Track.AUTHORIZATION_FIELD_NUMBER,
      Track.WAVEFORM_URLS_FIELD_NUMBER,
      Track.COUNTS_FIELD_NUMBER,
      Track.DOWNLOAD_METADATA_FIELD_NUMBER,
      Track.PUBLISHER_METADATA_FIELD_NUMBER,
      Track.AUDIO_ANALYSIS_FIELD_NUMBER
    )
  )

  val TrackVisibilityFieldMask: FieldMask = FieldMaskUtil.selectFieldNumbers[Track](
    Set(
      Track.METADATA_FIELD_NUMBER,
      Track.AUTHORIZATION_FIELD_NUMBER
    )
  )

  val RelatedTracksFieldMask: FieldMask = FieldMaskUtil.selectFieldNumbers[Track](
    Set(
      Track.METADATA_FIELD_NUMBER,
      Track.AUTHORIZATION_FIELD_NUMBER,
      Track.COUNTS_FIELD_NUMBER,
      Track.AUDIO_ANALYSIS_FIELD_NUMBER
    )
  )

  val TrackWithTranscodingsFieldMask: FieldMask = FieldMaskUtil.selectFieldNumbers[Track](
    Set(
      Track.METADATA_FIELD_NUMBER,
      Track.AUTHORIZATION_FIELD_NUMBER,
      Track.TRANSCODINGS_FIELD_NUMBER
    )
  )

  val TranscodingPaths: Set[String] =
    FieldMaskUtil.selectFieldNumbers[Track](Set(Track.TRANSCODINGS_FIELD_NUMBER)).paths.toSet
}
