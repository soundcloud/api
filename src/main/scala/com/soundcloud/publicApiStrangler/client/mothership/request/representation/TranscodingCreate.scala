package com.soundcloud.publicApiStrangler.client.mothership.request.representation

import play.api.libs.json.Json

case class TranscodingCreate(uid: String)

object TranscodingCreate {
  implicit val format = Json.format[TranscodingCreate]
}
