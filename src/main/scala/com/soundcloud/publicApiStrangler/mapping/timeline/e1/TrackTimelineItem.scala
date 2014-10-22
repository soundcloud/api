package com.soundcloud.publicApiStrangler.mapping.timeline.e1

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class TrackTimelineItem(jsonValue: JsValue,
                        entityMapper: EntityMapper,
                        entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(jsonValue) with TimelineItemWithUuid {

  val track = entityMapper.embed(Urn((json \ "urn").as[String]))
  val user =  entitySummaryMapper.embed(Urn((json \ "actor").as[String]))

}
