package com.soundcloud.publicApiStrangler.client.mothership.request.representation

import play.api.libs.json.Json

case class EmailUpdate(primary: Boolean)

object EmailUpdate {
  implicit val reads = Json.reads[EmailUpdate]
  implicit val writes = Json.writes[EmailUpdate]
}
