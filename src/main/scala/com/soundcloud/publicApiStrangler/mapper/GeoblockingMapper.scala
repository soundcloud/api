package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation.Geoblockings
import play.api.libs.json.JsValue

object GeoblockingMapper {
  def apply(json: JsValue): Option[Geoblockings] = (json \ "geo_blockings").asOpt[Geoblockings]
}
