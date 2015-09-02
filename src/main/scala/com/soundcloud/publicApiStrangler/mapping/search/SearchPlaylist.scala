package com.soundcloud.publicApiStrangler.mapping.search

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.search.PlaylistTracksMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapping.timeline.Playlist
import play.api.libs.json.JsValue

class SearchPlaylist(jsonValue: JsValue,
                     likeCountMapper: LikeCountMapper,
                     baseUrl: String,
                     playlistTracksMapper: PlaylistTracksMapper,
                     entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends Playlist(jsonValue: JsValue, Map.empty, baseUrl, entitySummaryMapper) {

  // we don't need these in search results
  override val secret_token = null
  override val secret_uri = null

  val tracks = playlistTracksMapper.embedAttr(urn, _.tracks)
  override val likes_count = Some(likeCountMapper.embedAttr(urn, _.like_count))
}
