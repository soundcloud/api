package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TracksCollection,
  TrackRepresentationLike
}
import com.twitter.util.Future

class UserTracksService(
    trackVisibilityService: TrackVisibilityService,
    trackRepresentationsService: TrackRepresentationsService,
    trackmetadataClient: TrackmetadataClient
) {

  def userTracks(
      session: UserSession,
      userUrn: Urn,
      trackPagination: TrackPagination
  ): Future[TracksCollection] = {
    for {
      trackUrns <- trackmetadataClient.urnsByUser(session, userUrn)
      trackUrnsPage = trackPagination.calculateTrackUrnPage(trackUrns).toList
      visibleTracks <- trackVisibilityService.tracks(
        session,
        trackUrnsPage.map(track => TrackRequest(track, None))
      )
      sortedVisibleTracks = trackPagination.calculateFinalPage(visibleTracks)
      enrichedTracks <- trackRepresentationsService.enrichTracks(session, sortedVisibleTracks)
    } yield {
      TracksCollection(enrichedTracks, trackPagination.nextHref(enrichedTracks.size))
    }
  }

  def userTrack(
      trackUrn: Urn,
      session: UserSession,
      userId: String,
      secretToken: Option[String]
  ): Future[Option[TrackRepresentationLike]] = {
    for {
      visibleTracks <- trackVisibilityService.tracks(session, List(TrackRequest(trackUrn, secretToken)))
      userOwnedTracks = visibleTracks.filter(_.userUrn.identifier == userId)
      enrichedTracks <- trackRepresentationsService.enrichTracks(session, userOwnedTracks)
    } yield {
      enrichedTracks.headOption
    }
  }
}
