package com.soudcloud.publicApiStrangler.mapping

import com.soudcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import play.api.libs.json.JsValue

abstract class TimelineItem(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext) extends JsonMapping(json) {

//  val tags = ???
//  val uuid = ???

  val created_at = (json \ "timestamp").as[String]
  val `type` = typeFor((json \ "type").as[String])


  private def typeFor(timelineType: String) = {
    timelineType.replace(":", "-")
  }
}
