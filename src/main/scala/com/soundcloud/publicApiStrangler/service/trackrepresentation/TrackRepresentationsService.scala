package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.WaveformUrlsGenerator
import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.tracks.{VisibleTrack, TrackRequest}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService
import com.twitter.util.Future
import scala.util.control.NonFatal

class TrackRepresentationsService(
    trackVisibilityService: TrackVisibilityService,
    okidokiClient: RichOkidokiClient,
    pubmeseClient: PubmeseClient,
    stitchClient: StitchClient,
    lieblingClient: LieblingClient,
    waveformUrlsGenerator: WaveformUrlsGenerator,
    userQuotaClient: UserQuotaClient
) {

  def tracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[TrackRepresentationLike]] = {
    for {
      visibleTracks <- trackVisibilityService.tracks(session, trackRequests)
      enrichedTracks <- enrichTracks(session, visibleTracks)
    } yield {
      enrichedTracks
    }
  }

  def track(
      session: UserSession,
      trackRequest: TrackRequest
  ): Future[Option[TrackRepresentationLike]] = tracks(session, List(trackRequest)).map(_.headOption)

  private def enrichTracks(
      session: UserSession,
      visibleTracks: List[VisibleTrack]
  ): Future[List[TrackRepresentationLike]] = {
    val urns = visibleTracks.map(_.urn).toSet
    val userUrns = visibleTracks.map(_.userUrn).toSet
    val builder = new TrackRepresentationBuilder
    val waveformUrls = visibleTracks.flatMap(_.uid).map(uid => uid -> waveformUrlsGenerator.fromUid(uid)).toMap
    val userUrnsFromLabelIds =
      visibleTracks.flatMap(_.labelId).map(labelId => Urn("soundcloud", "users", labelId.toString))
    Future
      .join(
        okidokiClient.fetchUserObjects(session, userUrns).map(users => users.map(user => user.urn -> user).toMap),
        okidokiClient.fetchTracksAudioMetadata(session, urns),
        session.user
          .map(user =>
            lieblingClient.userLikedTracks(session, urns, user).handle { case NonFatal(_) => Map.empty[Urn, Boolean] }
          )
          .getOrElse(Future.value(Map.empty[Urn, Boolean])),
        pubmeseClient.isrcsForTracks(session, urns).handle { case NonFatal(_) => Map.empty[Urn, Isrc] },
        okidokiClient.fetchTrackGeoblockings(session, urns).handle { case NonFatal(_) => Map.empty[Urn, Geoblockings] },
        okidokiClient.fetchTracksDomainLockings(session, urns).handle {
          case NonFatal(_) => Map.empty[Urn, List[DomainLocking]]
        },
        stitchClient.countsForTracks(session, visibleTracks.map(track => (track.userUrn, track.urn)).toSet).handle {
          case NonFatal(_) => Map.empty[Urn, StitchCounts]
        },
        okidokiClient.fetchUsersMap(session, userUrnsFromLabelIds.toSet).handle {
          case NonFatal(_) => Map.empty[Urn, User]
        },
        userQuotaClient.downloadsPerTrack(session, userUrns).handle { case NonFatal(_) => Map.empty[Urn, Option[Int]] }
      )
      .map {
        case (users, audios, isLiked, isrcs, geoBlockings, domainLockings, counts, labels, downloadsPerTrack) =>
          visibleTracks.map { visibleTrack =>
            builder.build(
              sessionUser = session.user,
              track = Track.fromVisibleTrack(visibleTrack),
              user = users(visibleTrack.userUrn),
              isrc = isrcs.get(visibleTrack.urn),
              counts = counts.get(visibleTrack.urn).getOrElse(StitchCounts(0, 0, 0, 0, 0)),
              label = visibleTrack.labelId.flatMap(id => labels.get(Urn("soundcloud", "users", id.toString))),
              geoblockings = geoBlockings.get(visibleTrack.urn).getOrElse(List.empty),
              domainLockings = domainLockings.get(visibleTrack.urn).getOrElse(List.empty),
              trackAudioMetadata = audios(visibleTrack.urn),
              isLiked = isLiked.get(visibleTrack.urn).getOrElse(false),
              waveformUrl = waveformUrls(visibleTrack.uid.getOrElse("")),
              downloadsPerTrack = downloadsPerTrack.get(visibleTrack.userUrn).flatten
            )
          }
      }
  }
}
