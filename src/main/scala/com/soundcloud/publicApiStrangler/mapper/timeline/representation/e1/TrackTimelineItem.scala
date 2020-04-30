package com.soundcloud.publicApiStrangler.mapper.timeline.representation.e1

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.JsValue

class TrackTimelineItem(jsonValue: JsValue, entityMapper: EntityMapper, entitySummaryMapper: EntitySummaryMapper)(
    implicit context: MappingContext
) extends JsonMapping(jsonValue)
    with TimelineItemWithUuid {
  val track = entityMapper.embed((json \ "urn").as[Urn])
  val user = entitySummaryMapper.embed((json \ "actor").as[Urn])
}
