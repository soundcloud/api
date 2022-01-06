package com.soundcloud.apipublic.mapper.similarsounds

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{JsLookupResult, JsObject, JsValue}

object SimilarSoundsMapper {
  def apply(json: JsValue): SimilarSounds = {
    SimilarSounds(mapTracks((json \ "tracks")), mapMeta(json \ "meta"))
  }

  private def mapTracks(json: JsLookupResult): Iterable[Urn] = {
    json.as[List[JsObject]].map { trackUrn =>
      (trackUrn \ "urn").as[Urn]
    }
  }

  private def mapMeta(json: JsLookupResult): SimilarSoundsMeta = {
    SimilarSoundsMeta(
      (json \ "page_size").as[Int],
      (json \ "variant").as[String],
      (json \ "source_version").as[String],
      (json \ "query_urn").as[Urn]
    )
  }
}
