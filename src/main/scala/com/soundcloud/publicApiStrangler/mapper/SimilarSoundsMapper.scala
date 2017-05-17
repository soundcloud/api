package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.representation.{SimilarSounds, SimilarSoundsMeta}
import play.api.libs.json.{JsLookupResult, JsObject, JsValue}

object SimilarSoundsMapper {

  def apply(json: JsValue): SimilarSounds = {
    SimilarSounds(
      mapTracks((json \ "tracks")),
      mapMeta(json \ "meta"))
  }

  private def mapTracks(json: JsLookupResult): Iterable[Urn] = {
    json.as[List[JsObject]].map {
      trackUrn =>
        new Urn((trackUrn \ "urn").as[String])
    }
  }

  private def mapMeta(json: JsLookupResult): SimilarSoundsMeta = {
    SimilarSoundsMeta(
      (json \ "page").as[Int],
      (json \ "page_size").as[Int],
      (json \ "variant").as[String],
      (json \ "source_version").as[String],
      new Urn((json \ "query_urn").as[String]),
      (json \ "previous_href").as[String],
      (json \ "next_href").as[String]
    )
  }
}
