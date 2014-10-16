package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import play.api.libs.json.JsValue

abstract class TimelineItem(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext) extends JsonMapping(json) {

  val uuid = (json \ "unique_id").asOpt[String]
  val created_at = (json \ "timestamp").as[String]
  val `type` = typeFor((json \ "type").as[String])

  // deprecated fields, kept for structure only
  val tags = None


  private def typeFor(timelineType: String) = {
    timelineType match {
      case "user:follow" => "affiliation"
      case "track:comment" => "comment"
      case other => timelineType.replace(":", "-")
    }

  }
}
