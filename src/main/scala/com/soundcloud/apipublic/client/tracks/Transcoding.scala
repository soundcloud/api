package com.soundcloud.apipublic.client.tracks

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
