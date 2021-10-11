package com.soundcloud.publicApiStrangler.handler.support.requestParser

import play.api.libs.json.Json

case class TrackAssetDataCreateRequest(original_filename: String, uid: String)

object TrackAssetDataCreateRequest extends TrackAssetRequestParams[TrackAssetDataCreateRequest] {
  implicit val writes = Json.writes[TrackAssetDataCreateRequest]
  val fileNameLength = 255
  def fromForm(params: Map[String, String]): Option[TrackAssetDataCreateRequest] =
    for {
      originalFileName <- params.get("original_filename")
      uid <- params.get("uid")
    } yield TrackAssetDataCreateRequest(originalFileName, uid)

  def isFileNameLengthWithInLimit(originalFileName: String): Boolean = originalFileName.length <= fileNameLength
}
