package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper._
import com.soundcloud.publicApiStrangler.client.tracks.TrackCoordinatorTrack
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateResult,
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

  def updateTrack(
      session: UserSession,
      trackUrn: Urn,
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      maybeUpdateTrackMetadata: Option[TrackMetadataUpdateRequest],
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): Future[Outcome[TrackCoordinatorTrack]] = {
    val requestBody = buildBody(maybeUpdateTrackAsset, maybeUpdateTrackMetadata, maybeArtworkMetadata)
    service
      .putWithSession(session, Path("/tracks") / trackUrn, Params.empty, Headers.empty, Some(requestBody))
      .map(TrackCoordinatorTrackMapper(_))
  }

  private def buildBody(
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      maybeUpdateTrackMetadata: Option[TrackMetadataUpdateRequest],
      maybeArtworkMetadata: Option[TrackArtworkUpdateResult]
  ): String = {
    var body = Json.obj()
    maybeUpdateTrackAsset.foreach(trackAsset => body = body ++ Json.toJson(trackAsset).as[JsObject])
    maybeUpdateTrackMetadata.foreach(metadata => body = body ++ Json.toJson(metadata.track).as[JsObject])
    maybeArtworkMetadata.foreach(artworkMeta => body = body ++ Json.obj("artwork_from_s3" -> Json.toJson(artworkMeta)))
    Json.stringify(body)
  }
}
