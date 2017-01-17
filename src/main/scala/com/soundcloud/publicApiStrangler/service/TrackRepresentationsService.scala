package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.controller.PublicApiPaginationParams
import com.soundcloud.publicApiStrangler.service.{TrackAccessibilityService, TrackRepresentationBuilder}

// FIXME: Do not use result types from Track Coordinator
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{Result, NotFound => TrackNotFound, Success => SuccessResult}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Track, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.representation._
import com.soundcloud.service.response.representation._
import com.twitter.util.{Future, NonFatal}

class TrackRepresentationsService(trackmetadataClient: TrackmetadataClient,
                                  okidokiClient: RichOkidokiClient,
                                  pubmeseClient: PubmeseClient,
                                  stitchClient: StitchClient,
                                  lieblingClient: LieblingClient,
                                  mediaUrlGenClient: MediaServiceUrlGenClient,
                                  userQuotaClient: UserQuotaClient,
                                  trackAccessibilityService: TrackAccessibilityService,
                                  trackRepresentationBuilder: TrackRepresentationBuilder = new TrackRepresentationBuilder) {

  def track(session: UserSession, urn: Urn, secretTokenInRequest: Option[String]): Future[Result[TrackRepresentationLike]] = {
    val isrcF = pubmeseClient.isrcForTrack(session, urn).handle { case NonFatal(ex) => None }
    val geoblockingsF = fetchGeoblockings(session, urn).handle { case NonFatal(ex) => None }
    val domainLockingsF = okidokiClient.fetchTrackDomainLockings(session, urn).handle { case NonFatal(ex) => Seq() }
    val audioF = okidokiClient.fetchTrackAudioMetadata(session, urn)

    trackmetadataClient.track(session, urn).flatMap {
      case Some(track) => trackAccessibilityService.isTrackAccessible(session, secretTokenInRequest, track).flatMap {
        case true => {
          val userForTrackF = fetchUser(track.user_urn, session)
          val labelF = track.label_id.map(labelId =>
            fetchUser(new Urn("soundcloud", "users", labelId.toString), session)).getOrElse(Future.value(None))
          val isLikedF = Option(session.getUser).map(user =>
            lieblingClient.userLikeCounts(session, List(track.urn), user).map(_.liked_track_urns.contains(track.urn))).getOrElse(Future.False)

          val waveformUrlsF = mediaUrlGenClient.waveformUrls(session, track.uid)
          val downloadsPerTrackF = userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)).map(_.get(track.user_urn).getOrElse(None))
          val countsF = userForTrackF.flatMap {
            case Some(user) => stitchClient.countsForTrack(session, urn, user.urn).map(Some(_)).liftToTry.map(_.getOrElse(None))
            case None => Future.value(None)
          }

          Future.join(userForTrackF, audioF, waveformUrlsF, isrcF, countsF, labelF, geoblockingsF, domainLockingsF, isLikedF, downloadsPerTrackF).map {
            case (Some(user), Some(audio), Some(waveformUrls), isrc, counts, label, geoblockings, domainLockings, isLiked, downloadsPerTrack) =>
              SuccessResult(trackRepresentationBuilder.build(
                sessionUser = Option(session.getUser),
                track = track,
                user = user,
                isrc = isrc,
                counts = counts.getOrElse(StitchCounts(0, 0, 0, 0, 0)),
                label = label,
                geoblockings = geoblockings,
                domainLockings = domainLockings,
                trackAudioMetadata = audio,
                isLiked = isLiked,
                waveformUrls = waveformUrls,
                secretTokenParameter = secretTokenInRequest,
                downloadsPerTrack = downloadsPerTrack
              ))
            case _ => TrackNotFound
          }
        }
        case _ => Future.value(TrackNotFound)
      }
      case _ => Future.value(TrackNotFound)
    }
  }

  def tracks(session: UserSession, userUrn: Urn, paginationParams: PublicApiPaginationParams): Future[List[TrackRepresentationLike]] = {
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
      val downloadsPerTrackF = fetchDownloadsPerTrack(userUrn, session)
      val accessibilityChecksF = trackAccessibilityService.areTracksAccessible(session, tracks)

      Future.join(labelsF, waveformUrlsF, downloadsPerTrackF, accessibilityChecksF)
    }

    for {
      (List(user), trackUrns) <- userAndAllTrackUrnsF
      trackUrnsPage = calculateTrackUrnPage(trackUrns)
      (tracks, isLiked, isrcs, geoblockings, domainLockings, audios, counts) <- allDependenciesOnlyOnTrackUrn(trackUrnsPage)
      (labels, waveformUrls, downloadsPerTrack, accessibilityCheck) <- allDependenciesOnTrackObjectList(tracks)
      sortedAccessibleTracks = tracks
        .filter(track => accessibilityCheck.get(track.urn).get) // the service should return values for all urns
        .sortBy(-_.urn.getIdentifier.toInt)
    } yield {
      sortedAccessibleTracks.flatMap(track => {
        val urn = track.urn

        for {
          audio <- audios.get(urn)
          uid <- track.uid
          waveformUrl <- waveformUrls.get(uid)
        } yield {
          trackRepresentationBuilder.build(
            sessionUser = Option(session.getUser),
            track = track,
            user = user,
            isrc = isrcs.get(urn),
            counts = counts.get(urn).getOrElse(StitchCounts(0, 0, 0, 0, 0)),
            label = labels.get(urn),
            geoblockings = geoblockings.get(urn),
            domainLockings = domainLockings.get(urn).getOrElse(List.empty),
            trackAudioMetadata = audio,
            isLiked = isLiked.get(urn).getOrElse(false),
            waveformUrls = waveformUrl,
            secretTokenParameter = None,
            downloadsPerTrack = downloadsPerTrack.get(urn).flatten
          )
        }
      })
    }
  }

  private def fetchGeoblockings(session: UserSession, urn: Urn): Future[Option[Geoblockings]] =
  // fetchTrackGeoblockings can return Some with zero geoblockings, which this method turns into None
    okidokiClient.fetchTrackGeoblockings(session, urn).map {
      case Some(geoblockings) => if (geoblockings.isEmpty) None else Some(geoblockings)
      case None => None
    }

  private def fetchUser(userUrn: Urn, session: UserSession): Future[Option[User]] =
    okidokiClient.fetchUserObjects(session, Set(userUrn)).map(_.headOption)


  private def fetchDownloadsPerTrack(userUrn: Urn, session: UserSession): Future[Map[Urn, Option[Int]]] =
    userQuotaClient.downloadsPerTrack(session, Set(userUrn))
}
