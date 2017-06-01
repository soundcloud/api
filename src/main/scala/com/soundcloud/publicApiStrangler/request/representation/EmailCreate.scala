package com.soundcloud.publicApiStrangler.request.representation

import play.api.libs.json.Json

case class EmailCreate(address: String)

object EmailCreate {
  implicit val format = Json.format[EmailCreate]
}
