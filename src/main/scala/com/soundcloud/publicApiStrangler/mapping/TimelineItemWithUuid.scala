package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import play.api.libs.json.JsValue

abstract class TimelineItemWithUuid(
                   json: JsValue,
                   entityMapper: EntityMapper)(implicit context: MappingContext)
  extends TimelineItem(json) {

  val uuid = (json \ "cursor").asOpt[String]

  // deprecated fields, kept for structure only
  val tags = None

}
