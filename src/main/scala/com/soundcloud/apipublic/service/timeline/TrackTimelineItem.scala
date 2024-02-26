package com.soundcloud.apipublic.service.timeline

import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentation
import play.api.libs.json.{JsObject, Json}

case class TrackTimelineItem(createdAt: String, timelineItemType: String, track: TrackRepresentation)
    extends TimelineItem(createdAt, timelineItemType) {

  def getRepresentation(): JsObject = {
    Json.obj("type" -> timelineItemType, "created_at" -> createdAt, "origin" -> Json.toJson(track))
  }
}
