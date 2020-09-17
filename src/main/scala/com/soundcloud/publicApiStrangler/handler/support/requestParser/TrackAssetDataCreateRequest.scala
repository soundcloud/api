package com.soundcloud.publicApiStrangler.handler.support.requestParser

import play.api.libs.json.Json

import scala.util.control.NonFatal

case class TrackAssetDataCreateRequest(original_filename: String, uid: String)

object TrackAssetDataCreateRequest extends TrackAssetRequestParams[TrackAssetDataCreateRequest] {
  implicit val writes = Json.writes[TrackAssetDataCreateRequest]

  def fromForm(params: Map[String, String]): Option[TrackAssetDataCreateRequest] = {
    try {
      Some(
        TrackAssetDataCreateRequest(
          original_filename = params.get("original_filename").get,
          uid = params.get("uid").get
        )
      )
    } catch {
      case NonFatal(_) => None
    }
  }
}
