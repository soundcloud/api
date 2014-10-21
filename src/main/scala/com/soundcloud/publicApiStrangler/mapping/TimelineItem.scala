package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.bff.nextbff.mapping.{MappingContext, JsonMapping}
import play.api.libs.json.JsValue


abstract class TimelineItem(json: JsValue)(implicit context: MappingContext) extends JsonMapping(json) {

  val created_at = (json \ "timestamp").as[String]
  val `type` = typeFor((json \ "type").as[String])

  private def typeFor(timelineType: String) = {
    timelineType match {
      case "user:follow" => "affiliation"
      case "track:comment" => "comment"
      case other => timelineType.replace(":", "-")
    }
  }
}
