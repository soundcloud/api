package com.soundcloud.apipublic.service.timeline

import play.api.libs.json.JsObject

abstract class TimelineItem(createdAt: String, timelineItemType: String) {
  def getRepresentation(): JsObject
}
