package com.soundcloud.publicApiStrangler.client.tracks

import play.api.libs.json.{Json, Reads}

sealed trait DownloadResponse

case class DownloadUrlResponse(url: String) extends DownloadResponse
case object DownloadErrorResponse extends DownloadResponse

object DownloadUrlResponse {
  implicit val reads: Reads[DownloadUrlResponse] = Json.reads[DownloadUrlResponse]
}
