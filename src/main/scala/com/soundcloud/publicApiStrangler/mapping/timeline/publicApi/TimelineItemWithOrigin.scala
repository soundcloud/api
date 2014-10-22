package com.soundcloud.publicApiStrangler.mapping.timeline.publicApi

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.TimelineItem
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class TimelineItemWithOrigin(json: JsValue,
                     entityMapper: EntityMapper,
                     entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(json) with TimelineItem {

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
