package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.json.UrnFormat._
import play.api.libs.json.{JsObject, JsValue}

object PlaylistTrackUrnsMapper {
  def apply(json: JsValue): List[Urn] = {
    (json \ "tracks").as[List[JsObject]]
      .map(trackJson => (trackJson \ "self" \ "urn").as[Urn])
  }
}
