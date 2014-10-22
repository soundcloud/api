package com.soundcloud.publicApiStrangler.mapping.timeline.e1

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class CommentTimelineItem(json: JsValue,
                          entityMapper: EntityMapper,
                          entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(json) with TimelineItemWithUuid {

  val comment = entityMapper.embed(Urn((json \ "urn").as[String]))

}
