package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper._
import com.soundcloud.publicApiStrangler.client.tracks.TrackMetadataUpdateResult
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

  def updateTrackAssetData(
      trackAssetDataUpdate: TrackAssetDataUpdateRequest,
      session: UserSession,
      trackUrn: Urn
  ): Future[Outcome[Unit]] = {
    val requestBody = Json.stringify(Json.toJson(trackAssetDataUpdate))

    service
      .putWithSession(session, Path("/tracks") / trackUrn, Params.empty, Headers.empty, Some(requestBody))
      .map(TrackCoordinatorResponseMapper(_))
  }

  def updateTrack(
      session: UserSession,
      trackUrn: Urn,
      updateTrackMetadata: Option[TrackMetadataUpdateRequest],
      artworkMetadata: Option[TrackArtworkUpdateResult]
  ): Future[Outcome[TrackMetadataUpdateResult]] = {

    val trackMetadataJson =
      (updateTrackMetadata, artworkMetadata) match {
        case (Some(trackMeta), Some(artworkMeta)) =>
          Json.toJson(trackMeta.track).as[JsObject] ++ Json.obj("artwork_from_s3" -> Json.toJson(artworkMeta))
        case (None, Some(artworkMeta)) => Json.obj("artwork_from_s3" -> Json.toJson(artworkMeta))
        case (Some(trackMeta), None) => Json.toJson(trackMeta.track)
        case _ => Json.obj()
      }

    val requestBody = Json.stringify(Json.toJson(trackMetadataJson))

    service
      .putWithSession(session, Path("/tracks") / trackUrn, Params.empty, Headers.empty, Some(requestBody))
      .map(TrackMetadataUpdateMapper(_))
  }
}
