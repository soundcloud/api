package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.FetchClient
import com.soundcloud.publicApiStrangler.client.support.ResponseHandlers.OptionalSingleItem
import com.soundcloud.publicApiStrangler.mapper.similarsounds.{SimilarSounds, SimilarSoundsMapper}
import com.twitter.util.Future
import play.api.libs.json.JsObject

/**
  * Client to fetch similar sounds given a seed sound
  */
class SimilarSoundsClient(service: JsonClient) extends FetchClient {

  /**
    * Fetches similar tracks to a given seed sound
    *
    * @param variant  might be specified to call different models for A/B testing
    * @param queryUrn uuid for tracking. Leave empty on first request. On successive requests use value returned from
    *                 client.
    * @return None if no similar tracks were found otherwise Some([[SimilarSounds]])
    */
  def fetchSimilar(
                    session: UserSession,
                    seedSoundUrn: Urn,
                    page: Int = 1,
                    pageSize: Int = 10,
                    variant: String = "",
                    queryUrn: Option[Urn] = None): Future[Option[SimilarSounds]] = {
    fetchSimilarTracks(session, seedSoundUrn, page, pageSize, variant, queryUrn).
      map(opt => opt.map(SimilarSoundsMapper(_)))
  }

  private def fetchSimilarTracks(
                                  session: UserSession,
                                  seedSoundUrn: Urn,
                                  page: Int,
                                  pageSize: Int,
                                  variant: String,
                                  queryUrn: Option[Urn]): Future[Option[JsObject]] = {

    val qUrn = queryUrn.map(_.toString).getOrElse("")
    fetch(
      service,
      session,
      Path() / "similar-to" / seedSoundUrn,
      Map("page_size" -> pageSize, "page" -> page, "variant" -> variant, "query_urn" -> qUrn),
      Headers.empty
    ).map(OptionalSingleItem(_))
  }

}
