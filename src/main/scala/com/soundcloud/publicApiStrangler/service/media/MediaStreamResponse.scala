package com.soundcloud.publicApiStrangler.service.media

import play.api.libs.json.{JsValue, Json, Writes}

sealed trait MediaStreamResponse
case class MediaStreamUrls(httpMp3: String, hlsMp3: String, hlsOpus: Option[String], httpPreviewMp3: String)
    extends MediaStreamResponse
case class MediaStreamUrl(httpMp3: String) extends MediaStreamResponse
case class PreviewUrls(httpMp3: String, hlsMp3: String) extends MediaStreamResponse
case object MediaStreamNotFoundError extends MediaStreamResponse

object MediaStreamResponse {
  implicit val writes = new Writes[MediaStreamResponse] {
    override def writes(resp: MediaStreamResponse): JsValue = resp match {
      case s: MediaStreamUrls =>
        val base = Json.obj(
          "http_mp3_128_url" -> s.httpMp3,
          "hls_mp3_128_url" -> s.hlsMp3
        )
        val withOpus = s.hlsOpus match {
          case Some(opus) => base ++ Json.obj("hls_opus_64_url" -> opus)
          case None => base
        }
        withOpus ++ Json.obj("preview_mp3_128_url" -> s.httpPreviewMp3)
      case MediaStreamUrl(url) => Json.obj("status" -> "302 - Found", "location" -> url)
      case p: PreviewUrls =>
        Json.obj(
          "http_mp3_128_url" -> p.httpMp3,
          "hls_mp3_128_url" -> p.hlsMp3
        )
      case MediaStreamNotFoundError => Json.obj()
    }
  }
}
