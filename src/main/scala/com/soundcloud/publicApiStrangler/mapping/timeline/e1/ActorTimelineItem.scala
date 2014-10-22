package com.soundcloud.publicApiStrangler.mapping.timeline.e1

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.EntityMapper
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class ActorTimelineItem(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext)
  extends JsonMapping(json) with TimelineItemWithUuid {

  val user = entityMapper.embed(Urn((json \ "actor").as[String]))

}
