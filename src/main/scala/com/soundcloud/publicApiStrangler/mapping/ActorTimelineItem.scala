package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class ActorTimelineItem(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext) extends TimelineItemWithUuid(json, entityMapper) {

  val user = entityMapper.embed(Urn((json \ "actor").as[String]))

}
