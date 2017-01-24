package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.util.ResultF
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.service.{TrackAccessibilityService, TrackPagination, TrackRepository, TrackRepresentationBuilder}

// FIXME: Do not use result types from Track Coordinator
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{Result, NotFound => TrackNotFound, Success => SuccessResult}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Track, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.representation._
import com.soundcloud.service.response.representation._
import com.twitter.util.{Future, NonFatal}

class TrackRepresentationsService(trackRepository: TrackRepository,
                                  trackmetadataClient: TrackmetadataClient,
                                  okidokiClient: RichOkidokiClient,
                                  pubmeseClient: PubmeseClient,
                                  stitchClient: StitchClient,
                                  lieblingClient: LieblingClient,
                                  mediaUrlGenClient: MediaServiceUrlGenClient,
                                  userQuotaClient: UserQuotaClient,
                                  trackAccessibilityService: TrackAccessibilityService,
                                  trackRepresentationBuilder: TrackRepresentationBuilder = new TrackRepresentationBuilder) {

  def track(session: UserSession, urn: Urn, secretTokenInRequest: Option[String]): Future[Result[TrackRepresentationLike]] = {

    def fetchGeoblockings(session: UserSession, urn: Urn): Future[Geoblockings] =
      // fetchTrackGeoblockings can return Some with zero geoblockings, which this method turns into None
      okidokiClient.fetchTrackGeoblockings(session, urn).map {
        case Some(geoblockings) => geoblockings
        case None => List.empty
      }

    def fetchUser(userUrn: Urn, session: UserSession): Future[Option[User]] =
      okidokiClient.fetchUserObjects(session, Set(userUrn)).map(_.headOption)


    val isrcF = pubmeseClient.isrcForTrack(session, urn).handle { case NonFatal(ex) => None }
    val geoblockingsF = fetchGeoblockings(session, urn).handle { case NonFatal(_) => List.empty }
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
            case (Some(user), Some(audio), waveformUrls, isrc, counts, label, geoblockings, domainLockings, isLiked, downloadsPerTrack) =>
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

  def tracks(session: UserSession, userUrn: Urn, paginationParams: TrackPagination): ResultF[TracksRepresentationResult] = {
    trackRepository.tracksByUser(session, userUrn, paginationParams).map { tracksResult => {
      val tracks = tracksResult.tracks.map { track =>
        val urn = track.urn
        for {
          audio <- tracksResult.audios.get(urn)
          uid <- track.uid
          waveformUrl <- tracksResult.waveformUrls.get(uid)
        } yield {
          trackRepresentationBuilder.build(
            sessionUser = Option(session.getUser),
            track = track,
            user = tracksResult.user,
            isrc = tracksResult.isrcs.get(urn),
            counts = tracksResult.counts.get(urn).getOrElse(StitchCounts(0, 0, 0, 0, 0)),
            label = tracksResult.labels.get(urn),
            geoblockings = tracksResult.geoblockings.get(urn).getOrElse(List.empty),
            domainLockings = tracksResult.domainLockings.get(urn).getOrElse(List.empty),
            trackAudioMetadata = audio,
            isLiked = tracksResult.isLiked.get(urn).getOrElse(false),
            waveformUrls = waveformUrl,
            secretTokenParameter = None,
            downloadsPerTrack = tracksResult.downloadsPerTrack.get(urn).flatten
          )
        }
      }.flatten

      TracksRepresentationResult(tracks, tracksResult.nextHref)
    }}
  }
}

case class TracksRepresentationResult(tracks: List[TrackRepresentationLike], nextHref: Option[String])
