package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.FetchClient
import com.soundcloud.publicApiStrangler.client.support.ResponseHandlers.OptionalSingleItem
import com.soundcloud.publicApiStrangler.mapper.similarsounds.{SimilarSounds, SimilarSoundsMapper}
import com.twitter.util.Future

class SystemPlaylistsClient(service: JsonClient) extends FetchClient {
  /**
    * Fetches all available similar tracks for a given seed track
    *
    * @return None if no similar tracks were found otherwise Some([[SimilarSounds]])
    */
  def fetchSimilar(session: UserSession, seedSoundUrn: Urn): Future[Option[SimilarSounds]] =
    fetch(service, session, Path() / "similar-sounds" / "get", Map("track_urn" -> seedSoundUrn.toString), Headers.empty)
      .map(OptionalSingleItem(_))
      .map(opt => opt.map(SimilarSoundsMapper(_)))
}
