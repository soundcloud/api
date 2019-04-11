package com.soundcloud.publicApiStrangler.mapper.timeline.representation.e1

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.EntityMapper
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.JsValue

class ActorTimelineItem(jsonValue: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext)
  extends JsonMapping(jsonValue) with TimelineItemWithUuid {

  val user = entityMapper.embed((json \ "actor").as[Urn])

}
