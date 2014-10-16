package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.publicApiStrangler.mapper.{EntitySummaryMapper, EntityMapper}
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.{JsObject, JsValue}

class Playlist(json: JsValue,
               likesByUrn: Map[Urn, Int],
               baseUrl: String,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends PlaylistSummary(json, baseUrl, entitySummaryMapper) {

  val likes_count = likesByUrn(urn)

  // deprecated fields, kept for structure only
  val downloadable = None
  val `type` = None
  val purchase_url = None
  val playlist_type = None
  val ean = None
  val purchase_title = None
  val created_with = None

}
