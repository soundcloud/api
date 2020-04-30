package com.soundcloud.publicApiStrangler.mapper.spotlight

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Self
import play.api.libs.json.JsValue

object SpotlightItemMapper {
  def apply(json: JsValue): SpotlightItem =
    SpotlightItem(
      Self((json \ "self" \ "urn").as[Urn], (json \ "self" \ "url").as[String]),
      Self((json \ "user" \ "urn").as[Urn], (json \ "user" \ "url").as[String]),
      (json \ "public").as[Boolean],
      (json \ "title").as[String],
      (json \ "last_modified").as[String]
    )
}
