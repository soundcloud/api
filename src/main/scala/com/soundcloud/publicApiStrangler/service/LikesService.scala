package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
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

  def userTracksLikesForUrns(
      session: UserSession,
      userUrn: Urn,
      trackUrns: List[Urn]
  ): Future[TracksCollection] = {
    for {
      likedTrackUrns <- lieblingClient.tracksLikedByUser(session, userUrn, trackUrns)
      enrichedTracks <- trackRepresentationsService.resolveTracks(session, likedTrackUrns)
    } yield {
      enrichedTracks
    }
  }
}
