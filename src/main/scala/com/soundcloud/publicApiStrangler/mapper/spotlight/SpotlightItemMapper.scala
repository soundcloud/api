package com.soundcloud.publicApiStrangler.mapper.spotlight

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.representation.Self
import com.soundcloud.publicApiStrangler.representation.spotlight.SpotlightItem
import play.api.libs.json.JsValue

object SpotlightItemMapper {

  def apply(json: JsValue): SpotlightItem =
    SpotlightItem(
      Self(Urn((json \ "self" \ "urn").as[String]), (json \ "self" \ "url").as[String]),
      Self(Urn((json \ "user" \ "urn").as[String]), (json \ "user" \ "url").as[String]),
      (json \ "public").as[Boolean],
      (json \ "title").as[String],
      (json \ "last_modified").as[String]
    )

}
