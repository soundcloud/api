package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Track, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.controller.PublicApiPaginationParams
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

  def tracks(session: UserSession,
             userUrn: Urn,
             paginationParams: PublicApiPaginationParams): Future[TracksResult] = {
    val userAndAllTrackUrnsF = Future.join(
      okidokiClient.fetchUserObjects(session, Set(userUrn)),
      trackmetadataClient.urnsByUser(session, userUrn))

    def calculateTrackUrnPage(allUserTrackUrns: List[Urn]) = {
      val start = paginationParams.offset.getOrElse(0)
      val end = start + paginationParams.limit.getOrElse(Int.MaxValue)
      // sort by id desc
      allUserTrackUrns.sortBy(-_.getIdentifier.toInt).slice(start, end).toSet
    }

    def allDependenciesOnlyOnTrackUrn(trackUrnsPage: Set[Urn]) = {
      val tracksF = trackmetadataClient.tracks(session, trackUrnsPage)
      val isLikedF = Option(session.getUser).map(user => lieblingClient.userLikedTracks(session, trackUrnsPage, user)).getOrElse(Future.value(Map.empty[Urn, Boolean]))
      val isrcsF = pubmeseClient.isrcsForTracks(session, trackUrnsPage).handle { case NonFatal(_) => Map.empty[Urn, Isrc] }
      val geoblockingsF = okidokiClient.fetchTrackGeoblockings(session, trackUrnsPage).handle { case NonFatal(_) => Map.empty[Urn, Geoblockings] }
      val domainLockingsF = okidokiClient.fetchTracksDomainLockings(session, trackUrnsPage).handle { case NonFatal(_) => Map.empty[Urn, List[DomainLocking]] }
      val audiosF = okidokiClient.fetchTracksAudioMetadata(session, trackUrnsPage).handle { case NonFatal(_) => Map.empty[Urn, TrackAudioMetadata] }
      val countsF = stitchClient.countsForTracksByUser(session, userUrn, trackUrnsPage).handle { case NonFatal(_) => Map.empty[Urn, StitchCounts] }

      Future.join(tracksF, isLikedF, isrcsF, geoblockingsF, domainLockingsF, audiosF, countsF)
    }

    def allDependenciesOnTrackObjectList(tracks: List[Track]) = {
      val labelsF = okidokiClient.fetchUsersMap(session, tracks.flatMap(_.label_id).map(labelId => Urn("soundcloud", "users", labelId.toString)).toSet)
      val waveformUrlsF = mediaUrlGenClient.waveformUrls(session, tracks.flatMap(_.uid)).map(_.getOrElse(Map.empty[String, Seq[WaveformUrl]]))
      val downloadsPerTrackF = userQuotaClient.downloadsPerTrack(session, Set(userUrn))
      val accessibilityChecksF = trackAccessibilityService.areTracksAccessible(session, tracks)

      Future.join(labelsF, waveformUrlsF, downloadsPerTrackF, accessibilityChecksF)
    }

    for {
      (List(user), trackUrns) <- userAndAllTrackUrnsF
      trackUrnsPage = calculateTrackUrnPage(trackUrns)
      (tracks, isLiked, isrcs, geoblockings, domainLockings, audios, counts) <- allDependenciesOnlyOnTrackUrn(trackUrnsPage)
      (labels, waveformUrls, downloadsPerTrack, accessibilityCheck) <- allDependenciesOnTrackObjectList(tracks)
      sortedAccessibleTracks = tracks
        .filter(track => accessibilityCheck.get(track.urn).get)
        .sortBy(-_.urn.getIdentifier.toInt)
    } yield {
      TracksResult(sortedAccessibleTracks,
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
        accessibilityCheck)
    }
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
                        accessibilityCheck: Map[Urn, Boolean])
