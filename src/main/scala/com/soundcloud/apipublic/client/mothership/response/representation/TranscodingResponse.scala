package com.soundcloud.apipublic.client.mothership.response.representation

import play.api.libs.json.Json

case class TranscodingResponse(status: String)

object TranscodingResponse {
  implicit val format = Json.format[TranscodingResponse]
}
