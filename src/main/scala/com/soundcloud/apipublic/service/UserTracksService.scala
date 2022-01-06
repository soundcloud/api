package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.chrono.ChronoItem
import com.soundcloud.apipublic.client.trackmetadata.TrackmetadataClient
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future

class UserTracksService(
    trackRepresentationsService: TrackRepresentationsService,
    trackmetadataClient: TrackmetadataClient
) {

  def userTracks(
      session: UserSession,
      userUrn: Urn,
      access: AccessParams,
      pagination: CursorBasedPagination
  ): Future[Collection[TrackRepresentation]] = {
    for {
      userTracksResponse <- trackmetadataClient.userTracks(session, userUrn, pagination)
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        userTracksResponse.items.map(item => TrackRequest(item.urn, None)),
        access
      )
    } yield {
      Collection(enrichedTracks, userTracksNextHref(userTracksResponse.items, pagination))
    }
  }

  def userTrack(
      trackUrn: Urn,
      session: UserSession,
      userId: String,
      secretToken: Option[String]
  ): Future[Option[TrackRepresentation]] = {
    for {
      track <- trackRepresentationsService.track(session, TrackRequest(trackUrn, secretToken))
      userOwnedTrack = track.filter(_.user.urn.identifier == userId)
    } yield {
      userOwnedTrack
    }
  }

  private def userTracksNextHref(items: List[ChronoItem], pagination: CursorBasedPagination): Option[String] = {
    if (items.nonEmpty) {
      Some(
        pagination
          .nextPage(items.last.cursor)
          .normalizedHref
      )
    } else {
      None
    }
  }
}
