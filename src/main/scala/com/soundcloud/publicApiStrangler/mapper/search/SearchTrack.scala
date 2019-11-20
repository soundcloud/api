package com.soundcloud.publicApiStrangler.mapper.search

import com.fasterxml.jackson.annotation.JsonIgnore
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.{WaveformMapper, WaveformRequestParams}
import com.soundcloud.publicApiStrangler.authorization.policies.ContentAuthorization
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.Track
import play.api.libs.json.JsValue

/**
  * Representation of a track as returned by search on public API.
  *
  * Similar to the track representation on timeline, but with some differences.
  */
class SearchTrack(session: UserSession,
                  jsonValue: JsValue,
                  likeCountMapper: LikeCountMapper,
                  repostCountsByUrn: Map[Urn, Long],
                  baseUrl: String,
                  entitySummaryMapper: EntitySummaryMapper,
                  @JsonIgnore contentAuthorization: ContentAuthorization,
                  waveform: WaveformMapper)
                 // Yep, that was my reaction, too.
                 (implicit if_this_is_named_context_then_serialization_fails: MappingContext)
  extends Track(jsonValue, Map.empty, repostCountsByUrn, baseUrl, entitySummaryMapper) {

  val download_url = if (hasDownloadLink)
    (json \ "download_url").asOpt[String]
  else
    None

  private def hasDownloadLink =
    downloadable.getOrElse(false) ||
      (json \ "user" \ "urn").as[Urn] == session.getUser

  // public API returns empty strings instead of nulls
  override val key_signature = Some("")

  override val user_favorite = Some(likeCountMapper.embedAttr(urn, _.did_user_like))
  override val likes_count = Some(likeCountMapper.embedAttr(urn, _.like_count))

  @JsonIgnore
  override val user_uri = ""
  override val release = (json \ "release").asOpt[String].orElse(Some(""))
  override val waveform_url = fetchWaveformUrl.orElse((json \ "waveform_url").asOpt[String])
  override val video_url = (json \ "video_url").asOpt[String]
  override val streamable = (json \ "api_streamable").asOpt[Boolean]

  private def fetchWaveformUrl =
    (json \ "uid").asOpt[String] map { uid =>
      waveform.embedAttr(WaveformRequestParams(uid, contentAuthorization.getPolicy), _.pngUrl)
    }

}
