package com.soundcloud.apipublic.handler.support.requestParser

import play.api.libs.json.Json

trait TrackAssetRequestParams[T] {
  def fromForm(params: Map[String, String]): Option[T]
  def isFileNameLengthWithInLimit(originalFileName: String): Boolean = originalFileName.length <= 255
}

case class TrackAssetDataUpdateRequest(replacing_original_filename: String, replacing_uid: String)

object TrackAssetDataUpdateRequest extends TrackAssetRequestParams[TrackAssetDataUpdateRequest] {
  implicit val writes = Json.writes[TrackAssetDataUpdateRequest]

  def fromForm(params: Map[String, String]): Option[TrackAssetDataUpdateRequest] =
    for {
      originalFileNameUpdate <- params.get("original_filename")
      uidUpdate <- params.get("uid")
    } yield TrackAssetDataUpdateRequest(originalFileNameUpdate, uidUpdate)
}
