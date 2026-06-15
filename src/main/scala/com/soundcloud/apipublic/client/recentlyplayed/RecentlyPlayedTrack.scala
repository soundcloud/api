package com.soundcloud.apipublic.client.recentlyplayed

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration}

case class RecentlyPlayedTrack(urn: Urn, playedAt: Long)

object RecentlyPlayedTrack {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val reads = Json.reads[RecentlyPlayedTrack]
  implicit val writes = Json.writes[RecentlyPlayedTrack]
}
