package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentationLike,
  TrackRepresentationsService,
  TracksCollection
}
import com.twitter.util.Future

class LikesService(
    trackRepresentationsService: TrackRepresentationsService,
    lieblingClient: LieblingClient
) {

  def userTrackLikeForUrn(
      session: UserSession,
      userUrn: Urn,
      trackUrn: Urn
  ): Future[Option[TrackRepresentationLike]] = {
    for {
      likedTrackUrns <- lieblingClient.userTracksLikesForUrns(session, userUrn, List(trackUrn))
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        likedTrackUrns.map(track => TrackRequest(track, None))
      )
    } yield {
      enrichedTracks.headOption
    }
  }

  def userTracksLikes(
      session: UserSession,
      userUrn: Urn,
      pagination: CursorBasedPagination
  ): Future[TracksCollection] = {
    for {
      likesPage <- lieblingClient.userTracksLikes(session, userUrn, pagination.cursor, pagination.pageSize)
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        likesPage.likes.map(like => TrackRequest(like.target_urn, None))
      )
    } yield {
      val nextHref =
        likesPage.meta.cursor.next_params.map(params => pagination.nextPage(params.cursor)).map(_.normalizedHref)

      TracksCollection(enrichedTracks, nextHref)
    }
  }
}
