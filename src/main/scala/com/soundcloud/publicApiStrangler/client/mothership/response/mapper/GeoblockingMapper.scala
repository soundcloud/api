package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Geoblockings
import play.api.libs.json.JsValue

object GeoblockingMapper {
  def apply(json: JsValue): Option[Geoblockings] = (json \ "geo_blockings").asOpt[Geoblockings]
}
