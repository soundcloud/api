package com.soundcloud.publicApiStrangler.mapping.timeline.e1

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class CommentTimelineItem(jsonValue: JsValue,
                          entityMapper: EntityMapper,
                          entitySummaryMapper: EntitySummaryMapper)(implicit contex_with_a_different_name: MappingContext)
  extends JsonMapping(jsonValue) with TimelineItemWithUuid {

  val comment = entityMapper.embed(Urn((json \ "urn").as[String]))
  val user =  entitySummaryMapper.embed(Urn((json \ "actor").as[String]))

  override def isValid = user.isValid
}
