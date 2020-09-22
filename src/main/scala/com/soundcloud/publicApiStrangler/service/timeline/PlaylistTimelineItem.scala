package com.soundcloud.publicApiStrangler.service.timeline

import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import play.api.libs.json.{JsObject, Json}

class PlaylistTimelineItem(createdAt: String, timelineItemType: String, playlist: Playlist)
    extends TimelineItem(createdAt, timelineItemType) {

  def getRepresentation(): JsObject = {
    Json.obj("type" -> timelineItemType, "created_at" -> createdAt, "origin" -> Json.toJson(playlist))
  }
}
