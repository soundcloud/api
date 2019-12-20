package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.WaveformUrlsGenerator
import com.soundcloud.publicApiStrangler.client.mothership.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{
  Result,
  NotFound => TrackNotFound,
  Success => SuccessResult
}
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.service.TrackAccessibilityService
import com.soundcloud.publicApiStrangler.support.ResultF
import com.twitter.util.Future

import scala.util.control.NonFatal

class TrackRepresentationsService(
    trackRepository: TrackRepository,
    trackmetadataClient: TrackmetadataClient,
    okidokiClient: RichOkidokiClient,
    pubmeseClient: PubmeseClient,
    stitchClient: StitchClient,
    lieblingClient: LieblingClient,
    waveformUrlsGenerator: WaveformUrlsGenerator,
    userQuotaClient: UserQuotaClient,
    trackAccessibilityService: TrackAccessibilityService,
    trackRepresentationBuilder: TrackRepresentationBuilder = new TrackRepresentationBuilder
) {
  def track(
      session: UserSession,
      urn: Urn,
      secretTokenInRequest: Option[String]
  ): Future[Result[TrackRepresentationLike]] = {
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
      case Some(track) =>
        trackAccessibilityService.isTrackAccessible(session, secretTokenInRequest, track).flatMap {
          case true => {
            val userForTrackF = fetchUser(track.user_urn, session)
            val labelF = track.label_id
              .map(labelId => fetchUser(Urn("soundcloud", "users", labelId.toString), session))
              .getOrElse(Future.value(None))
            val isLikedF = Option(session.getUser)
              .map(user =>
                lieblingClient
                  .userLikeCounts(session, List(track.urn), user)
                  .map(_.liked_track_urns.contains(track.urn))
              )
              .getOrElse(Future.False)

            val downloadsPerTrackF =
              userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)).map(_.get(track.user_urn).getOrElse(None))
            val countsF = userForTrackF.flatMap {
              case Some(user) =>
                stitchClient.countsForTrack(session, urn, user.urn).map(Some(_)).liftToTry.map(_.getOrElse(None))
              case None => Future.value(None)
            }

            Future
              .join(
                userForTrackF,
                audioF,
                isrcF,
                countsF,
                labelF,
                geoblockingsF,
                domainLockingsF,
                isLikedF,
                downloadsPerTrackF
              )
              .map {
                case (
                    Some(user),
                    Some(audio),
                    isrc,
                    counts,
                    label,
                    geoblockings,
                    domainLockings,
                    isLiked,
                    downloadsPerTrack
                    ) =>
                  track.uid match {
                    case Some(uid) =>
                      SuccessResult(
                        trackRepresentationBuilder.build(
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
                          waveformUrl = waveformUrlsGenerator.fromUid(uid),
                          secretTokenParameter = secretTokenInRequest,
                          downloadsPerTrack = downloadsPerTrack
                        )
                      )
                    case None => TrackNotFound
                  }

                case _ => TrackNotFound
              }
          }
          case _ => Future.value(TrackNotFound)
        }
      case _ => Future.value(TrackNotFound)
    }
  }

  def tracks(
      session: UserSession,
      userUrn: Urn,
      paginationParams: TrackPagination
  ): ResultF[TracksRepresentationResult] = {
    trackRepository.tracksByUser(session, userUrn, paginationParams).map { tracksResult =>
      {
        val tracks = tracksResult.tracks.flatMap { track =>
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
              waveformUrl = waveformUrl,
              secretTokenParameter = None,
              downloadsPerTrack = tracksResult.downloadsPerTrack.get(urn).flatten
            )
          }
        }

        TracksRepresentationResult(tracks, tracksResult.nextHref)
      }
    }
  }
}

case class TracksRepresentationResult(tracks: List[TrackRepresentationLike], nextHref: Option[String])
