package com.soundcloud.apipublic.service.media

import play.api.libs.json.{JsValue, Json, Writes}

trait MediaStreamResponse
case class RedirectStreamResponse(url: String) extends MediaStreamResponse
case class MediaStreamUrls(
    httpMp3: Option[String] = None,
    hlsMp3: Option[String] = None,
    hlsAac96k: Option[String] = None,
    hlsAac160k: Option[String] = None,
    httpPreviewMp3: Option[String] = None
) extends MediaStreamResponse

object MediaStreamResponse {
  implicit val writes = new Writes[MediaStreamResponse] {
    override def writes(resp: MediaStreamResponse): JsValue = resp match {
      case RedirectStreamResponse(httpMp3) => Json.obj("status" -> "302 - Found", "location" -> httpMp3)
      case MediaStreamUrls(httpMp3, hlsMp3, hlsAac96k, hlsAac160k, httpPreviewMp3) => {
        val parts = Seq(
          httpMp3.map(u => Json.obj("http_mp3_128_url" -> u)),
          hlsMp3.map(u => Json.obj("hls_mp3_128_url" -> u)),
          hlsAac160k.map(hls => Json.obj("hls_aac_160_url" -> hls)),
          hlsAac96k.map(hls => Json.obj("hls_aac_96k_url" -> hls)),
          httpPreviewMp3.map(preview => Json.obj("preview_mp3_128_url" -> preview))
        ).flatten
        parts.foldLeft(Json.obj())(_ ++ _)
      }
    }
  }
}
