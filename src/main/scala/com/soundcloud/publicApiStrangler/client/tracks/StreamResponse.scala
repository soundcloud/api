package com.soundcloud.publicApiStrangler.client.tracks

import play.api.libs.json.{Json, Reads}

sealed trait StreamResponse

case class StreamUrlResponse(url: String, mimeType: String) extends StreamResponse

object StreamUrlResponse {
  implicit val reads: Reads[StreamUrlResponse] = Json.reads[StreamUrlResponse]
}
case object StreamErrorResponse extends StreamResponse
