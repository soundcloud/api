package com.soundcloud.publicApiStrangler.mapper.spotlight

import play.api.libs.json.{JsObject, JsValue}

object SpotlightMapper {
  def apply(json: JsValue): Spotlight =
    Spotlight(json.as[List[JsObject]].map(SpotlightItemMapper(_)))
}
