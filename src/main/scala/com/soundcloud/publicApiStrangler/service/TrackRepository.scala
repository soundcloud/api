package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.ResultF.joinF
import com.soundcloud.jvmkit.module.util.{Bad, Error, Good, Result, ResultF}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Track, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.service.response.representation.{Geoblockings, User}
import com.twitter.util.{Future, NonFatal}

class TrackRepository(trackmetadataClient: TrackmetadataClient,
                      okidokiClient: RichOkidokiClient,
                      pubmeseClient: PubmeseClient,
                      stitchClient: StitchClient,
                      lieblingClient: LieblingClient,
                      mediaUrlGenClient: MediaServiceUrlGenClient,
                      userQuotaClient: UserQuotaClient,
                      trackAccessibilityService: TrackAccessibilityService) {

  def tracksByUser(session: UserSession,
                   userUrn: Urn,
                   trackPagination: TrackPagination): ResultF[TracksResult] = {

    val userAndAllTrackUrnsF = joinF(
      toResult(okidokiClient.fetchUserObjects(session, Set(userUrn)), "Could not load the tracks' owner"),
      toResult(trackmetadataClient.urnsByUser(session, userUrn), "Could not load the tracks' urns"))

    for {
      (List(user), trackUrns) <- userAndAllTrackUrnsF
      trackUrnsPage = trackPagination.calculateTrackUrnPage(trackUrns)
      (tracks, isLiked, isrcs, geoblockings, domainLockings, audios, counts) <- allDependenciesOnlyOnTrackUrn(session, userUrn, trackUrnsPage)
      (labels, waveformUrls, downloadsPerTrack, accessibilityCheck) <- allDependenciesOnTrackObjectList(session, userUrn, tracks)
    } yield {
      val accessibleTracks = tracks
        .filter(track => accessibilityCheck.get(track.urn).get)
        .filter(track => audios.get(track.urn).map(_.state == TrackAudioMetadata.FinishedState).getOrElse(true))

      val sortedAccessibleTracks = trackPagination.calculateFinalPage(accessibleTracks)

      val nextHref = trackPagination.nextHref(trackUrns.size)

      TracksResult(
        sortedAccessibleTracks,
        user,
        isLiked,
        isrcs,
        geoblockings,
        domainLockings,
        audios,
        counts,
        labels,
        waveformUrls,
        downloadsPerTrack,
        nextHref)
    }
  }

  private def allDependenciesOnlyOnTrackUrn(session: UserSession, userUrn: Urn, trackUrnsPage: Set[Urn]) = {
    val tracksF = toResult(trackmetadataClient.tracks(session, trackUrnsPage), "Could not load tracks from trackmetadata")
    val audiosF = toResult(okidokiClient.fetchTracksAudioMetadata(session, trackUrnsPage), "Could not load the audio information")

    val isLikedF = Option(session.getUser)
      .map(user => toResult(lieblingClient.userLikedTracks(session, trackUrnsPage, user), Map.empty[Urn, Boolean]))
      .getOrElse(Future.value(Good(Map.empty[Urn, Boolean])))
    val isrcsF = toResult(pubmeseClient.isrcsForTracks(session, trackUrnsPage), Map.empty[Urn, Isrc])
    val geoblockingsF = toResult(okidokiClient.fetchTrackGeoblockings(session, trackUrnsPage), Map.empty[Urn, Geoblockings])
    val domainLockingsF = toResult(okidokiClient.fetchTracksDomainLockings(session, trackUrnsPage), Map.empty[Urn, List[DomainLocking]])
    val countsF = toResult(stitchClient.countsForTracksByUser(session, userUrn, trackUrnsPage), Map.empty[Urn, StitchCounts])


    joinF(tracksF, isLikedF, isrcsF, geoblockingsF, domainLockingsF, audiosF, countsF)
  }

  private def allDependenciesOnTrackObjectList(session: UserSession, userUrn: Urn, tracks: List[Track]) = {
    val waveformUrlsF = toResult(mediaUrlGenClient.waveformUrls(session, tracks.flatMap(_.uid)), "Could not load the tracks' waveforms")

    val userUrnsFromLabelIds = tracks.flatMap(_.label_id).map(labelId => Urn("soundcloud", "users", labelId.toString))
    val labelsF = toResult(okidokiClient.fetchUsersMap(session, userUrnsFromLabelIds.toSet), Map.empty[Urn, User])
    val downloadsPerTrackF = toResult(userQuotaClient.downloadsPerTrack(session, Set(userUrn)), Map.empty[Urn, Option[Int]])
    val accessibilityChecksF: Future[Result[Map[Urn, Boolean]]] = toResult(
      trackAccessibilityService.areTracksAccessible(session, tracks),
      tracks.map(t => (t.urn, false)).toMap) // defaults to not accessible if something goes wrong

    joinF(labelsF, waveformUrlsF, downloadsPerTrackF, accessibilityChecksF)
  }

  private def toResult[T](future: Future[T], errorMessage: String): Future[Result[T]] = {
    future.map(Good(_)).handle { case NonFatal(e) => Bad(Error(errorMessage, e)) }
  }
  private def toResult[T](future: Future[T], default: T): Future[Result[T]] = {
    future
      .map(Good(_))
      .handle { case NonFatal(_) => Good(default) }
  }
}

case class TracksResult(tracks: List[Track],
                        user: User,
                        isLiked: Map[Urn, Boolean],
                        isrcs: Map[Urn, Isrc],
                        geoblockings: Map[Urn, Geoblockings],
                        domainLockings: Map[Urn, List[DomainLocking]],
                        audios: Map[Urn, TrackAudioMetadata],
                        counts: Map[Urn, StitchCounts],
                        labels: Map[Urn, User],
                        waveformUrls: Map[String, Seq[WaveformUrl]],
                        downloadsPerTrack: Map[Urn, Option[Int]],
                        nextHref: Option[String])
