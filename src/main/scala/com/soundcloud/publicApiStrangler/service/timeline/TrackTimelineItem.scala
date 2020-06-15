package com.soundcloud.publicApiStrangler.service.timeline

import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import play.api.libs.json.{JsObject, Json}

class TrackTimelineItem(createdAt: String, tags: List[String], track: TrackRepresentation)
    extends TimelineItem(createdAt, tags) {

  def getRepresentation(): JsObject = {
    Json.obj("type" -> "track", "created_at" -> createdAt, "tags" -> tags.mkString(","), "origin" -> Json.toJson(track))
  }
}
