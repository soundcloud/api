package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TracksCollection,
  TrackRepresentation
}
import com.twitter.util.Future

class UserTracksService(
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
      tracks <- trackRepresentationsService.tracks(session, trackUrnsPage.map(TrackRequest(_, None)))
      finalPage = trackPagination.calculateFinalPage(tracks)
    } yield {
      TracksCollection(finalPage, trackPagination.nextHref(finalPage.size))
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
      userOwnedTrack = track.filter(_.track.user_urn.identifier == userId)
    } yield {
      userOwnedTrack
    }
  }
}
