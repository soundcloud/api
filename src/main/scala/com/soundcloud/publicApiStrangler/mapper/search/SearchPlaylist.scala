package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.Playlist
import play.api.libs.json.JsValue

class SearchPlaylist(
    jsonValue: JsValue,
    likeCountMapper: LikeCountMapper,
    repostCountsByUrn: Map[Urn, Long],
    baseUrl: String,
    entitySummaryMapper: EntitySummaryMapper
)(implicit context: MappingContext)
    extends Playlist(jsonValue: JsValue, Map.empty, repostCountsByUrn, baseUrl, entitySummaryMapper) {
  // we don't need these in search results
  override val secret_token = null
  override val secret_uri = null

  override val likes_count = Some(likeCountMapper.embedAttr(urn, _.like_count))
}
