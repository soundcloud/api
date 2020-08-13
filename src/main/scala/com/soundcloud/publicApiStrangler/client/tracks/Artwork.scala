package com.soundcloud.publicApiStrangler.client.tracks

import play.api.libs.json.Json

case class Artwork(filename: Option[String])

object Artwork {
  implicit val format = Json.format[Artwork]
}
