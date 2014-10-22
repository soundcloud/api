package com.soundcloud.publicApiStrangler.mapping.timeline.e1

import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.publicApiStrangler.mapping.timeline.TimelineItem

trait TimelineItemWithUuid extends TimelineItem {
  self: JsonMapping =>

  val uuid = (json \ "cursor").asOpt[String]

  // deprecated fields, kept for structure only
  val tags = None

}
