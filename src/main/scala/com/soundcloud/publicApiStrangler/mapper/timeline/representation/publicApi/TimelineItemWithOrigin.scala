package com.soundcloud.publicApiStrangler.mapper.timeline.representation.publicApi

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.TimelineItem
import play.api.libs.json.JsValue

class TimelineItemWithOrigin(jsonValue: JsValue, entityMapper: EntityMapper, entitySummaryMapper: EntitySummaryMapper)(
    implicit context: MappingContext
) extends JsonMapping(jsonValue)
    with TimelineItem {
  val origin = entityMapper.embed(originUrn)

  // deprecated fields, kept for structure only
  val tags = None

  private def originUrn = {
    (json \ "urn").as[Urn] match {
      case Urn(_, "affiliations", _) => (json \ "actor").as[Urn]
      case other => other
    }
  }
}
