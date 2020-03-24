package com.soundcloud.api.partners.clients.tracks

import play.api.libs.json.{JsSuccess, Reads}

case class Transcoding(
    uuid: String,
    preset: String,
    mimeType: String,
    protocols: List[String],
    protocolsSnippet: Option[List[String]],
    quality: String,
    durationMs: Long,
    durationSnippetMs: Option[Long]
)

object Transcoding {

  implicit val reads: Reads[Transcoding] = Reads[Transcoding] { json =>
    JsSuccess(
      Transcoding(
        uuid = (json \ "uuid").as[String],
        preset = (json \ "preset").as[String],
        mimeType = (json \ "mimeType").as[String],
        protocols = (json \ "protocols").as[List[String]],
        protocolsSnippet = (json \ "protocolsSnippet").asOpt[List[String]],
        quality = (json \ "quality").as[String],
        durationMs = (json \ "durationMs").as[Long],
        durationSnippetMs = (json \ "durationSnippetMs").asOpt[Long]
      )
    )
  }
}
