package com.soundcloud.publicApiStrangler.mapper.search

import com.fasterxml.jackson.annotation.JsonIgnore
import com.soundcloud.bff.Future
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapping.search.PlaylistTracks
import com.soundcloud.publicApiStrangler.support.mapping.{IndividualFetch, InputValidation, ObjectMapping}
import com.soundcloud.scalakit._
import com.soundcloud.service.client.OkidokiClient

class PlaylistTracksMapper(okidokiClient: OkidokiClient, baseUrl: String)
  extends Mapper[Urn, PlaylistTracks]
  with InputValidation[Urn, PlaylistTracks]
  with IndividualFetch[Urn, PlaylistTracks] {

  mapper =>

  // XXX: 1:N calls here :(
  override def mapSingleInput(session: UserSession, input: Urn)(implicit context: MappingContext): Future[PlaylistTracks] = {
    // XXX: what about pagination? Do we just return the first N tracks per playlist?
    okidokiClient.playlistTracks(session, input).map { response =>
      new ObjectMapping(response.tracks) with PlaylistTracks {
        @JsonIgnore override def baseUrl: String = mapper.baseUrl
      }
    }
  }
}


