package com.soundcloud.publicApiStrangler.mapping.search

import com.fasterxml.jackson.annotation.JsonIgnore
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.purchaselink.TrackPurchaseLinkMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.{WaveformMapper, WaveformRequestParams}
import com.soundcloud.publicApiStrangler.mapping.timeline.Track
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

/**
 * Representation of a track as returned by search on public API.
 *
 * Similar to the track representation on timeline, but with some differences.
 */
class SearchTrack(session: UserSession,
                   jsonValue: JsValue,
                   likeCountMapper: LikeCountMapper,
                   baseUrl: String,
                   entitySummaryMapper: EntitySummaryMapper,
                   @JsonIgnore contentAuthorization: ContentAuthorization,
                   waveform: WaveformMapper,
                   trackPurchaseLinkMapper: TrackPurchaseLinkMapper)
                  // Yep, that was my reaction, too.
                  (implicit if_this_is_named_context_then_serialization_fails: MappingContext)
  extends Track(jsonValue, Map.empty, baseUrl, entitySummaryMapper) {

     val download_url = if (hasDownloadLink)
       (json \ "download_url").asOpt[String]
     else
       None

     private def hasDownloadLink =
       downloadable.getOrElse(false) ||
       Urn((json \ "user" \ "urn").as[String]) == session.getUser

     // public API returns empty strings instead of nulls
     override val key_signature = Some("")

     override val user_favorite = Some(likeCountMapper.embedAttr(urn, _.did_user_like))
     override val likes_count = Some(likeCountMapper.embedAttr(urn, _.like_count))

     @JsonIgnore
     override val user_uri = ""
     override val release = (json \ "release").asOpt[String].orElse(Some(""))
     override val attachments_uri = Some(uri + "/attachments")
     override val waveform_url = fetchWaveformUrl.orElse((json \ "waveform_url").asOpt[String])
     override val purchase_url = Some(trackPurchaseLinkMapper.embedAttr(urn, _.url))
     override val video_url = (json \ "video_url").asOpt[String]

     private def fetchWaveformUrl =
       (json \ "uid").asOpt[String] map { uid =>
         waveform.embedAttr(WaveformRequestParams(uid, contentAuthorization.getPolicy), _.pngUrl)
       }

   }
