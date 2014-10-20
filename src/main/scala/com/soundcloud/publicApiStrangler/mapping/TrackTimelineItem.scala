package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.{EntitySummaryMapper, EntityMapper}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class TrackTimelineItem(json: JsValue,
                        entityMapper: EntityMapper,
                        entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends TimelineItem(json, entityMapper) {

  val track = entityMapper.embed(Urn((json \ "urn").as[String]))
  val user =  entitySummaryMapper.embed(Urn((json \ "actor").as[String]))

}
