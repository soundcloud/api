package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{JsObject, JsValue}
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats.urnFormat

object PlaylistTrackUrnsMapper {
  def apply(json: JsValue): List[Urn] = {
    (json \ "tracks").as[List[JsObject]]
      .map(trackJson => (trackJson \ "self" \ "urn").as[Urn])
  }
}
