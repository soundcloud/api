package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.client.profile.ProfilesClient
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future
import proto.soundcloud.profiles.api.ChronoItem

class UserTracksService(
    trackRepresentationsService: TrackRepresentationsService,
    profilesClient: ProfilesClient
) {

  def userTracks(
      session: UserSession,
      userUrn: Urn,
      access: AccessParams,
      pagination: CursorBasedPagination
  ): Future[Collection[TrackRepresentation]] = {
    for {
      userTracksResponse <- profilesClient.fetchTracksUploadedByUserFromProfiles(
        session,
        userUrn,
        pagination.pageSize,
        pagination.cursor.getOrElse("")
      )
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        userTracksResponse.items.toList.map(item => TrackRequest(Urn.parse(item.urn).get, Option.empty)),
        access
      )
    } yield {
      Collection(enrichedTracks, userTracksNextHref(userTracksResponse.items, pagination))
    }
  }

  private def userTracksNextHref(items: Seq[ChronoItem], pagination: CursorBasedPagination): Option[String] = {
    if (items.nonEmpty) {
      Some(
        pagination
          .nextPage(items.last.cursor)
          .normalizedHref
      )
    } else {
      Option.empty
    }
  }
}
