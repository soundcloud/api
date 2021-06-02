package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.WaveformUrlsGenerator
import com.soundcloud.publicApiStrangler.client.mothership.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Geoblockings
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService
import com.twitter.util.Future

import scala.util.control.NonFatal

class TrackRepresentationsService(
    trackVisibilityService: TrackVisibilityService,
    okidokiClient: RichOkidokiClient,
    pubmeseClient: PubmeseClient,
    lieblingClient: LieblingClient,
    waveformUrlsGenerator: WaveformUrlsGenerator
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
      visibleTracks <- trackVisibilityService.visibleTracks(session, trackRequests, access)
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
    val waveformUrls = visibleTracks.flatMap(_.uid).map(uid => uid -> waveformUrlsGenerator.fromUid(uid)).toMap

    Future
      .join(
        okidokiClient.fetchUserObjects(session, userUrns).map(users => users.map(user => user.urn -> user).toMap),
        session.user
          .map(user =>
            lieblingClient.userLikedTracks(session, urns, user).handle { case NonFatal(_) => Map.empty[Urn, Boolean] }
          )
          .getOrElse(Future.value(Map.empty[Urn, Boolean])),
        pubmeseClient.isrcsForTracks(session, urns).handle { case NonFatal(_) => Map.empty[Urn, Isrc] },
        okidokiClient.fetchTrackGeoblockings(session, urns).handle { case NonFatal(_) => Map.empty[Urn, Geoblockings] }
      )
      .map {
        case (users, isLiked, isrcs, geoBlockings) =>
          visibleTracks.map { visibleTrack =>
            TrackRepresentationBuilder.fromVisibleTrack(
              client = session.agent,
              sessionUser = session.user,
              visibleTrack = visibleTrack,
              user = users(visibleTrack.userUrn),
              isrc = isrcs.get(visibleTrack.urn),
              geoblockings = geoBlockings.getOrElse(visibleTrack.urn, List.empty),
              isLiked = isLiked.getOrElse(visibleTrack.urn, false),
              waveformUrl = waveformUrls(visibleTrack.uid.getOrElse(""))
            )
          }
      }
  }
}
