package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.{EntitySummaryMapper, EntityMapper}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class CommentTimelineItem(json: JsValue,
                          entityMapper: EntityMapper,
                          entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends TimelineItem(json, entityMapper) {

  val comment = entityMapper.embed(Urn((json \ "urn").as[String]))

}
