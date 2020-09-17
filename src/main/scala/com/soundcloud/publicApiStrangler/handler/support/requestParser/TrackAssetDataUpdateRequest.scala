package com.soundcloud.publicApiStrangler.handler.support.requestParser

import play.api.libs.json.Json

import scala.util.control.NonFatal

trait TrackAssetRequestParams[T] {
  def fromForm(params: Map[String, String]): Option[T]
}

case class TrackAssetDataUpdateRequest(replacing_original_filename: String, replacing_uid: String)

object TrackAssetDataUpdateRequest extends TrackAssetRequestParams[TrackAssetDataUpdateRequest] {
  implicit val writes = Json.writes[TrackAssetDataUpdateRequest]

  def fromForm(params: Map[String, String]): Option[TrackAssetDataUpdateRequest] = {
    try {
      Some(
        TrackAssetDataUpdateRequest(
          replacing_original_filename = params.get("original_filename").get,
          replacing_uid = params.get("uid").get
        )
      )
    } catch {
      case NonFatal(_) => None
    }
  }
}
