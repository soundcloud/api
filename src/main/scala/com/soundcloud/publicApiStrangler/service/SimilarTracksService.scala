package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.SystemPlaylistsClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationLike,
  TrackRepresentationsService,
  TracksCollection
}
import com.twitter.util.Future

class SimilarTracksService(
    trackVisibilityService: TrackVisibilityService,
    trackRepresentationsService: TrackRepresentationsService,
    systemPlaylistsClient: SystemPlaylistsClient
) {

  def similarTracks(
      session: UserSession,
      trackUrn: Urn,
      trackPagination: TrackPagination
  ): Future[Option[TracksCollection]] = {
    for {
      similarTracks <- systemPlaylistsClient.fetchSimilar(session, trackUrn)
      trackUrns = similarTracks.map(similarTrack => similarTrack.similarTracks).getOrElse(List.empty).toList
      trackUrnsPage = trackPagination.calculateTrackUrnPage(trackUrns).toList
      visibleTracks <- trackVisibilityService.tracks(
        session,
        trackUrnsPage.map(track => TrackRequest(track, None))
      )
      sortedVisibleTracks = trackPagination.calculateFinalPage(visibleTracks)
      enrichedTracks <- trackRepresentationsService.enrichTracks(session, sortedVisibleTracks)
    } yield {
      enrichedTracks match {
        case tracks: List[TrackRepresentationLike] if !tracks.isEmpty =>
          Some(TracksCollection(tracks, trackPagination.nextHref(enrichedTracks.size)))
        case _ =>
          None
      }
    }
  }
}
