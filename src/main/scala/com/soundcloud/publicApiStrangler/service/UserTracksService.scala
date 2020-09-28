package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.chrono.ChronoItem
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future

class UserTracksService(
    trackRepresentationsService: TrackRepresentationsService,
    trackmetadataClient: TrackmetadataClient
) {

  def userTracks(
      session: UserSession,
      userUrn: Urn,
      pagination: CursorBasedPagination
  ): Future[Collection[TrackRepresentation]] = {
    for {
      userTracksResponse <- trackmetadataClient.userTracks(session, userUrn, pagination)
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        userTracksResponse.items.map(item => TrackRequest(item.urn, None))
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
      userOwnedTrack = track.filter(_.visibleTrack.userUrn.identifier == userId)
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
