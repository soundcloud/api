package com.soundcloud.apipublic.client

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.support.FetchClient
import com.soundcloud.apipublic.client.support.ResponseHandlers.OptionalSingleItem
import com.soundcloud.apipublic.mapper.similarcreators.SimilarCreatorsMapper
import com.soundcloud.apipublic.mapper.similarsounds.{SimilarSounds, SimilarSoundsMapper}
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

  /**
    * Fetches related artist user URNs for a seed user (see system playlists similar-creators).
    *
    * @return None on backend 404, otherwise Some (possibly empty list of users).
    */
  def fetchSimilarCreators(session: UserSession, userUrn: Urn, pageSize: Int) =
    fetch(
      service,
      session,
      Path() / "similar-creators" / "get",
      Map("user_urn" -> userUrn.toString, "page_size" -> pageSize.toString),
      Headers.empty
    ).map(OptionalSingleItem(_))
      .map(opt => opt.map(SimilarCreatorsMapper(_)))
}
