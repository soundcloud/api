package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationLike,
  TrackRepresentationsService,
  TracksCollection
}
import com.twitter.util.Future

class LikesService(
    trackRepresentationsService: TrackRepresentationsService,
    lieblingClient: LieblingClient
) {
  def userTracksLikes(
      session: UserSession,
      userUrn: Urn,
      trackPagination: TrackPagination
  ): Future[TracksCollection] = {
    for {
      trackUrns <- lieblingClient.userTracksLikes(session, userUrn)
      enrichedTracks <- trackRepresentationsService.resolveTracks(session, trackUrns, trackPagination)
    } yield {
      enrichedTracks
    }
  }

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
}
