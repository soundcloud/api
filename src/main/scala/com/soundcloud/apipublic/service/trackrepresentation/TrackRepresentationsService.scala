package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.apipublic.client.mothership.RichOkidokiClient
import com.soundcloud.apipublic.client.mothership.response.representation.Geoblockings
import com.soundcloud.apipublic.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService
import com.soundcloud.apipublic.service.TrackVisibilityService.DefaultTrackFieldMask
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future

import scala.util.control.NonFatal

class TrackRepresentationsService(
    trackVisibilityService: TrackVisibilityService,
    okidokiClient: RichOkidokiClient,
    likedTracksService: LikedTracksService
) {

  def track(
      session: UserSession,
      trackRequest: TrackRequest
  ): Future[Option[TrackRepresentation]] =
    tracks(session, List(trackRequest), AccessParams.explicitAccess).map(_.headOption)

  def tracks(
      session: UserSession,
      trackRequests: List[TrackRequest],
      access: AccessParams
  ): Future[List[TrackRepresentation]] = {
    for {
      visibleTracks <- trackVisibilityService.visibleTracks(session, trackRequests, DefaultTrackFieldMask, access)
      enrichedTracks <- enrichTracks(session, visibleTracks)
    } yield {
      enrichedTracks
    }
  }

  private def enrichTracks(
      session: UserSession,
      visibleTracks: List[VisibleTrack]
  ): Future[List[TrackRepresentation]] = {
    val urns = visibleTracks.map(_.urn).toSet
    val userUrns = visibleTracks.map(_.userUrn).toSet

    Future
      .join(
        okidokiClient.fetchUserObjects(session, userUrns).map(users => users.map(user => user.urn -> user).toMap),
        likedTracksService.getLikedTracks(session, urns.toSeq),
        okidokiClient.fetchTrackGeoblockings(session, urns).handle { case NonFatal(_) => Map.empty[Urn, Geoblockings] }
      )
      .map {
        case (users, isLiked, geoBlockings) =>
          visibleTracks.map { visibleTrack =>
            TrackRepresentationBuilder.fromVisibleTrack(
              client = session.agent,
              sessionUser = session.user,
              visibleTrack = visibleTrack,
              user = users(visibleTrack.userUrn),
              geoblockings = geoBlockings.getOrElse(visibleTrack.urn, List.empty),
              isLiked = isLiked.getOrElse(visibleTrack.urn, false)
            )
          }
      }
  }
}
