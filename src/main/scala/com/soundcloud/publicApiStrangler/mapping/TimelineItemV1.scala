package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class TimelineItemV1(json: JsValue,
                     entityMapper: EntityMapper,
                     entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends JsonMapping(json) {

  val created_at = (json \ "timestamp").as[String]
  val `type` = typeFor((json \ "type").as[String])
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
