package com.soundcloud.publicApiStrangler.request.representation

import play.api.libs.json.Json

case class TranscodingCreate(uid: String)

object TranscodingCreate {
  implicit val format = Json.format[TranscodingCreate]
}
