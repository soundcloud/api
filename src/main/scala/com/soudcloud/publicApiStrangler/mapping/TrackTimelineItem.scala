package com.soudcloud.publicApiStrangler.mapping

import com.soudcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class TrackTimelineItem(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext) extends TimelineItem(json, entityMapper) {

  val track = entityMapper.embed(Urn((json \ "urn").as[String]))

}
