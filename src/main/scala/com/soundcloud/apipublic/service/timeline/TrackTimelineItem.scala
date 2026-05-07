package com.soundcloud.apipublic.service.timeline

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentation
import play.api.libs.json.{JsObject, Json}

case class TrackTimelineItem(
    createdAt: String,
    timelineItemType: String,
    track: TrackRepresentation,
    reposterUrn: Option[Urn] = None
) extends TimelineItem(createdAt, timelineItemType) {

  def getRepresentation(): JsObject = {
    val base = Json.obj("type" -> timelineItemType, "created_at" -> createdAt, "origin" -> Json.toJson(track))
    reposterUrn.fold(base)(urn => base + ("reposter" -> Json.toJson(urn.toString)))
  }
}
