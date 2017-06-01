package com.soundcloud.publicApiStrangler.mapper.timeline.representation.publicApi

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.TimelineItem
import play.api.libs.json.JsValue

class TimelineItemWithOrigin(jsonValue: JsValue,
                             entityMapper: EntityMapper,
                             entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(jsonValue) with TimelineItem {

  val origin = entityMapper.embed(originUrn)

  // deprecated fields, kept for structure only
  val tags = None


  private def originUrn = {
    Urn((json \ "urn").as[String]) match {
      case Urn(_, "affiliations", _) => Urn((json \ "actor").as[String])
      case other => other
    }
  }

  private def typeFor(timelineType: String) = {
    timelineType match {
      case "user:follow" => "affiliation"
      case "track:comment" => "comment"
      case other => timelineType.replace(":", "-")
    }

  }
}
