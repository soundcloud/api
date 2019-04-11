package com.soundcloud.publicApiStrangler.service.media

import play.api.libs.json.{JsValue, Json, Writes}

sealed trait StreamResponse
case class StreamUrls(httpMp3: String, hlsMp3: String, hlsOpus: Option[String], httpPreviewMp3: String) extends StreamResponse
case class StreamUrl(httpMp3: String) extends StreamResponse
case class PreviewUrls(httpMp3: String, hlsMp3: String) extends StreamResponse
case object StreamNotFoundError extends StreamResponse

object StreamResponse {
  implicit val writes = new Writes[StreamResponse] {
    override def writes(resp: StreamResponse): JsValue = resp match {
      case s:StreamUrls =>
        val base = Json.obj(
          "http_mp3_128_url" -> s.httpMp3,
          "hls_mp3_128_url" -> s.hlsMp3,
        )
        val withOpus = s.hlsOpus match {
          case Some(opus) => base ++ Json.obj("hls_opus_64_url" -> opus)
          case None => base
        }
        withOpus ++ Json.obj("preview_mp3_128_url" -> s.httpPreviewMp3)
      case StreamUrl(url) => Json.obj("status" -> "302 - Found", "location" -> url)
      case p:PreviewUrls => Json.obj(
          "http_mp3_128_url" -> p.httpMp3,
          "hls_mp3_128_url" -> p.hlsMp3,
        )
      case StreamNotFoundError => Json.obj()
    }
  }
}
