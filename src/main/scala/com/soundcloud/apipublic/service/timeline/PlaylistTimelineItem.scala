package com.soundcloud.apipublic.service.timeline

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.service.playlists.representation.Playlist
import play.api.libs.json.{JsObject, Json}

case class PlaylistTimelineItem(
    createdAt: String,
    timelineItemType: String,
    playlist: Playlist,
    reposterUrn: Option[Urn] = None
) extends TimelineItem(createdAt, timelineItemType) {

  def getRepresentation(): JsObject = {
    val base = Json.obj("type" -> timelineItemType, "created_at" -> createdAt, "origin" -> Json.toJson(playlist))
    reposterUrn.fold(base)(urn => base + ("reposter" -> Json.toJson(urn.toString)))
  }
}
