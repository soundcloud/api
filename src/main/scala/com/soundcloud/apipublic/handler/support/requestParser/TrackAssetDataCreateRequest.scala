package com.soundcloud.apipublic.handler.support.requestParser

import play.api.libs.json.Json

case class TrackAssetDataCreateRequest(original_filename: String, uid: String)

object TrackAssetDataCreateRequest extends TrackAssetRequestParams[TrackAssetDataCreateRequest] {
  implicit val writes = Json.writes[TrackAssetDataCreateRequest]

  def fromForm(params: Map[String, String]): Option[TrackAssetDataCreateRequest] =
    for {
      originalFileName <- params.get("original_filename")
      uid <- params.get("uid")
    } yield TrackAssetDataCreateRequest(originalFileName, uid)
}
