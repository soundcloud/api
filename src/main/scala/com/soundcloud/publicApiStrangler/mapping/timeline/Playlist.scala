package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.service.response.representation.liebling.LikesCount
import play.api.libs.json.JsValue

class Playlist(jsonValue: JsValue,
               likesCounts: Seq[LikesCount],
               baseUrl: String,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends PlaylistSummary(jsonValue, baseUrl, entitySummaryMapper) {

  val likes_count: Option[Any] = likesCounts.find(_.target_urn == urn).map(_.likes_count)

  // deprecated fields, kept for structure only
  val downloadable = None
  val `type` = None
  val purchase_url = None
  val playlist_type = None
  val ean = None
  val purchase_title = None
  val created_with = None
}
