package com.soundcloud.apipublic.service.media

import play.api.libs.json.{JsValue, Json, Writes}

trait MediaStreamResponse
case class RedirectStreamResponse(url: String) extends MediaStreamResponse
case class MediaStreamUrls(
    httpMp3: Option[String] = None,
    hlsMp3: Option[String] = None,
    hlsAac96k: Option[String] = None,
    hlsAac160k: Option[String] = None,
    hlsOpus: Option[String] = None,
    httpPreviewMp3: Option[String] = None
) extends MediaStreamResponse

object MediaStreamResponse {
  implicit val writes = new Writes[MediaStreamResponse] {
    override def writes(resp: MediaStreamResponse): JsValue = resp match {
      case RedirectStreamResponse(httpMp3) => Json.obj("status" -> "302 - Found", "location" -> httpMp3)
      case MediaStreamUrls(httpMp3, hlsMp3, hlsAac96k, hlsAac160k, hlsOpus, httpPreviewMp3) => {
        val opus = hlsOpus.map(opus => Json.obj("hls_opus_64_url" -> opus)).getOrElse(Json.obj())
        val hls160k = hlsAac160k.map(hls => Json.obj("hls_aac_160_url" -> hls)).getOrElse(Json.obj())
        val hls96k = hlsAac96k.map(hls => Json.obj("hls_aac_96k_url" -> hls)).getOrElse(Json.obj())
        val preview = httpPreviewMp3.map(preview => Json.obj("preview_mp3_128_url" -> preview)).getOrElse(Json.obj())
        Json.obj(
          "http_mp3_128_url" -> httpMp3,
          "hls_mp3_128_url" -> hlsMp3
        ) ++ hls160k ++ hls96k ++ opus ++ preview
      }
    }
  }
}
