package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.EntityMapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class PlaylistTimelineItem(json: JsValue, entityMapper: EntityMapper)(implicit context: MappingContext) extends TimelineItem(json, entityMapper) {

  val playlist = entityMapper.embed(Urn((json \ "urn").as[String]))

}
