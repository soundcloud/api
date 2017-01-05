package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import play.api.libs.json.JsValue

class Playlist(jsonValue: JsValue,
               likesByUrn: Map[Urn, Long],
               repostCountsByUrn: Map[Urn, Long],
               baseUrl: String,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends PlaylistSummary(jsonValue, repostCountsByUrn, baseUrl, entitySummaryMapper) {

  val likes_count: Option[Any] = likesByUrn.get(urn)

  // deprecated fields, kept for structure only
  val downloadable = None
  val `type` = None
  val purchase_url = None
  val playlist_type = None
  val ean = None
  val purchase_title = None
  val created_with = None

}
