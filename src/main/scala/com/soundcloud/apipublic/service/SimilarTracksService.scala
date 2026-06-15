package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.SystemPlaylistsClient
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService.RelatedTracksFieldMask
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentation,
  TrackRepresentationsService
}
import com.twitter.util.Future

class SimilarTracksService(
    trackRepresentationsService: TrackRepresentationsService,
    systemPlaylistsClient: SystemPlaylistsClient
) {

  def similarTracks(
      session: UserSession,
      trackUrn: Urn,
      access: AccessParams,
      trackPagination: TrackPagination
  ): Future[Option[Collection[TrackRepresentation]]] = {
    for {
      similarTracks <- systemPlaylistsClient.fetchSimilar(session, trackUrn)
      trackUrns = similarTracks.map(similarTrack => similarTrack.similarTracks).getOrElse(List.empty).toList
      trackUrnsPage = trackPagination.calculateTrackUrnPage(trackUrns).toList
      tracks <- trackRepresentationsService.tracks(
        session,
        trackUrnsPage.map(TrackRequest(_, None)),
        access,
        fieldMask = RelatedTracksFieldMask
      )
      finalPage = trackPagination.calculateFinalPage(tracks)
    } yield {
      finalPage match {
        case _: List[TrackRepresentation] if finalPage.nonEmpty =>
          Some(Collection(finalPage, trackPagination.nextHref(finalPage.size)))
        case _ =>
          None
      }
    }
  }
}
