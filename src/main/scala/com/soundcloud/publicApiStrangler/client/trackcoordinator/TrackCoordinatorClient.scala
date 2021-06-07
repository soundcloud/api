package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper._
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateResult,
  TrackAssetDataCreateRequest,
  TrackAssetDataUpdateRequest,
  TrackMetadataCreateRequest,
  TrackMetadataRequest,
  TrackMetadataUpdateRequest
}
import com.soundcloud.publicApiStrangler.service.users.UserUploadQuota
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
      trackMetadata: TrackMetadataCreateRequest,
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): Future[Outcome[TrackCoordinatorTrack]] = {
    val requestBody = buildCreateBody(trackAsset, trackMetadata, maybeArtworkMetadata)
    service
      .postWithSession(session, Path("/tracks"), Params.empty, Headers.empty, Some(requestBody))
      .map(TrackCoordinatorCreateMapper(_))
  }

  def updateTrack(
      session: UserSession,
      trackUrn: Urn,
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      updateTrackMetadata: TrackMetadataUpdateRequest,
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): Future[Outcome[TrackCoordinatorTrack]] = {
    val requestBody = buildUpdateBody(maybeUpdateTrackAsset, updateTrackMetadata, maybeArtworkMetadata)
    service
      .putWithSession(session, Path("/tracks") / trackUrn, Params.empty, Headers.empty, Some(requestBody))
      .map(TrackCoordinatorUpdateMapper(_))
  }

  def uploadQuota(session: UserSession, userUrn: Urn): Future[Outcome[UserUploadQuota]] = {
    service
      .getWithSession(
        session,
        Path("/user/upload-quota"),
        Params.empty,
        Headers(TrackCoordinatorHeaders.USER -> userUrn.toString)
      )
      .map(UserUploadQuotaMapper(_))
  }

  private def buildCreateBody(
      trackAsset: TrackAssetDataCreateRequest,
      trackMetadata: TrackMetadataCreateRequest,
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): String = {
    val body = Json.toJson(trackAsset).as[JsObject]
    bodyString(body, trackMetadata, maybeArtworkMetadata)
  }

  private def buildUpdateBody(
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      maybeUpdateTrackMetadata: TrackMetadataUpdateRequest,
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): String = {
    var body = Json.obj()
    maybeUpdateTrackAsset.foreach(trackAsset => body = body ++ Json.toJson(trackAsset).as[JsObject])
    bodyString(body, maybeUpdateTrackMetadata, maybeArtworkMetadata)
  }

  private def bodyString(
      assetBody: JsObject,
      trackMetadata: TrackMetadataRequest,
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): String = {
    var body = assetBody

    body = body ++ Json.toJson(trackMetadata.getTrack).as[JsObject]
    maybeArtworkMetadata.foreach(artworkMeta => body = body ++ Json.obj("artwork_from_s3" -> Json.toJson(artworkMeta)))
    Json.stringify(body)
  }
}

object TrackCoordinatorHeaders {
  val USER = "Sc-User"
}
