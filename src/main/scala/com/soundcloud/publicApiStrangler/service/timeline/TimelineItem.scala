package com.soundcloud.publicApiStrangler.service.timeline

import play.api.libs.json.JsObject

abstract class TimelineItem(createdAt: String) {
  def getRepresentation(): JsObject
}
