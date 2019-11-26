package com.soundcloud.publicApiStrangler.mapper.timeline.representation.e1

import java.util.UUID

import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.TimelineItem

trait TimelineItemWithUuid extends TimelineItem {
  self: JsonMapping =>

  val uuid = (json \ "cursor").asOpt[String].map(UUID.fromString)

  // deprecated fields, kept for structure only
  val tags = None
}
