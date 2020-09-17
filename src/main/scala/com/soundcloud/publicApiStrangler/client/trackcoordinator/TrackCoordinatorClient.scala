package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper._
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateResult,
  TrackAssetDataCreateRequest,
  TrackAssetDataUpdateRequest,
  TrackMetadataUpdateRequest
}
import com.twitter.util.Future
import play.api.libs.json.{JsObject, Json}

class TrackCoordinatorClient(service: JsonClient) {
  def deleteTrack(session: UserSession, trackUrn: Urn): Future[Outcome[Unit]] = {
    service
      .deleteWithSession(session, Path("/tracks") / trackUrn, Params.empty, Headers.empty(), None)
      .map(TrackCoordinatorResponseMapper(_))
  }

  def createTrack(
      session: UserSession,
      trackAsset: TrackAssetDataCreateRequest,
      maybeUpdateTrackMetadata: Option[TrackMetadataUpdateRequest],
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): Future[Outcome[TrackCoordinatorTrack]] = {
    val requestBody = buildCreateBody(trackAsset, maybeUpdateTrackMetadata, maybeArtworkMetadata)
    service
      .postWithSession(session, Path("/tracks"), Params.empty, Headers.empty, Some(requestBody))
      .map(TrackCoordinatorTrackMapper(_))
  }

  def updateTrack(
      session: UserSession,
      trackUrn: Urn,
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      maybeUpdateTrackMetadata: Option[TrackMetadataUpdateRequest],
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): Future[Outcome[TrackCoordinatorTrack]] = {
    val requestBody = buildUpdateBody(maybeUpdateTrackAsset, maybeUpdateTrackMetadata, maybeArtworkMetadata)
    service
      .putWithSession(session, Path("/tracks") / trackUrn, Params.empty, Headers.empty, Some(requestBody))
      .map(TrackCoordinatorTrackMapper(_))
  }

  private def buildCreateBody(
      trackAsset: TrackAssetDataCreateRequest,
      maybeTrackMetadata: Option[TrackMetadataUpdateRequest],
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): String = {
    val body = Json.toJson(trackAsset).as[JsObject]
    bodyString(body, maybeTrackMetadata, maybeArtworkMetadata)
  }

  private def buildUpdateBody(
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      maybeUpdateTrackMetadata: Option[TrackMetadataUpdateRequest],
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): String = {
    var body = Json.obj()
    maybeUpdateTrackAsset.foreach(trackAsset => body = body ++ Json.toJson(trackAsset).as[JsObject])
    bodyString(body, maybeUpdateTrackMetadata, maybeArtworkMetadata)
  }

  private def bodyString(
      assetBody: JsObject,
      maybeUpdateTrackMetadata: Option[TrackMetadataUpdateRequest],
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): String = {
    var body = assetBody
    maybeUpdateTrackMetadata.foreach(metadata => body = body ++ Json.toJson(metadata.track).as[JsObject])
    maybeArtworkMetadata.foreach(artworkMeta => body = body ++ Json.obj("artwork_from_s3" -> Json.toJson(artworkMeta)))
    Json.stringify(body)
  }
}
