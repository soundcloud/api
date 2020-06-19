package com.soundcloud.publicApiStrangler.service.timeline

import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import play.api.libs.json.{JsObject, Json}

class TrackTimelineItem(createdAt: String, track: TrackRepresentation) extends TimelineItem(createdAt) {

  def getRepresentation(): JsObject = {
    Json.obj("type" -> "track", "created_at" -> createdAt, "origin" -> Json.toJson(track))
  }
}
