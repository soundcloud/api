package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistsClient
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}

// FIXME: Do not use result types from Track Coordinator
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{Result, NotFound => TrackNotFound, Success => SuccessResult}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Track, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.representation._
import com.soundcloud.service.response.representation._
import com.twitter.util.{Future, NonFatal}

import com.soundcloud.jvmkit.ModuleConversions._

class TrackRepresentationsService(
                                   trackmetadataClient: TrackmetadataClient,
                                   okidokiClient: RichOkidokiClient,
                                   pubmeseClient: PubmeseClient,
                                   stitchClient: StitchClient,
                                   lieblingClient: LieblingClient,
                                   mediaUrlGenClient: MediaServiceUrlGenClient,
                                   userQuotaClient: UserQuotaClient,
                                   playlistClient: PlaylistsClient) {

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass.getName)

  def track(session: UserSession, urn: Urn, secretTokenInRequest: Option[String]): Future[Result[TrackRepresentationLike]] = {
    val isrcF = pubmeseClient.isrcForTrack(session, urn).handle { case NonFatal(ex) => None }
    val geoblockingsF = fetchGeoblockings(session, urn).handle { case NonFatal(ex) => None }
    val domainlockingsF = okidokiClient.fetchTrackDomainLockings(session, urn).handle { case NonFatal(ex) => Seq() }
    val audioF = okidokiClient.fetchTrackAudioMetadata(session, urn)

    trackmetadataClient.track(session, urn).flatMap {
      case Some(track) => isTrackAccessible(session, secretTokenInRequest, track).flatMap {
        case true => {
          val userF = fetchUserForTrack(track, session)
          val labelF = fetchLabelForTrack(track, session)
          val isLikedF = fetchUserLikesTrack(track, session)
          val waveformUrlsF = fetchWaveformUrls(track, session)
          val downloadsPerTrackF = fetchDownloadsPerTrack(track, session)
          val countsF = userF.flatMap {
            // FIXME: Overly complicated
            case Some(user) => stitchClient.countsForTrack(session, urn, user.urn).map(Some(_)).liftToTry.map(_.getOrElse(None))
            case None => Future.value(None)
          }

          Future.join(userF, audioF, waveformUrlsF, isrcF, countsF, labelF, geoblockingsF, domainlockingsF, isLikedF, downloadsPerTrackF).map {
            case (Some(user), Some(audio), Some(waveformUrls), isrc, counts, label, geoblockings, domainlockings, isLiked, downloadsPerTrack) =>
              SuccessResult(buildTrackRepresentationLike(
                userSession = session,
                track = track,
                user = user,
                isrc = isrc,
                counts = getCounts(counts),
                label = label,
                geoblockings = geoblockings,
                domainlockings = domainlockings,
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

  private def buildTrackRepresentationLike(
                                            userSession: UserSession,
                                            track: Track,
                                            user: User,
                                            isrc: Option[Isrc],
                                            counts: StitchCounts,
                                            label: Option[User],
                                            geoblockings: Option[Geoblockings],
                                            domainlockings: Seq[DomainLocking],
                                            trackAudioMetadata: TrackAudioMetadata,
                                            isLiked: Boolean,
                                            waveformUrls: Seq[WaveformUrl],
                                            secretTokenParameter: Option[String],
                                            downloadsPerTrack: Option[Int]
                                          ): TrackRepresentationLike = {
    val basicTrackRep = TrackRepresentation(
      track = track,
      user = user,
      isrc = isrc,
      counts = counts,
      label = label,
      geoblockings = geoblockings,
      domainlockings = domainlockings,
      audioMetadata = trackAudioMetadata
    )

    // Temporary logging for 'downloadable' difference debugging
    logger.info(s"Track: ${track.urn}. Downloadable: ${track.downloadable}. " +
      s"Downloads per track: ${downloadsPerTrack}. " +
      s"Download count: ${counts.download_count}.")

    val userIsOwner = track.user_urn == userSession.getUser

    var rep: TrackRepresentationLike = basicTrackRep
    // TODO Consider an "owning user" decorator
    if (userIsOwner)
      rep = TrackRepresentationSecretTokenDecorator(track, rep)
    if (userIsOwner || track.reveal_stats)
      rep = TrackRepresentationCountsDecorator(counts, rep)
    if ((userIsOwner || track.reveal_stats) && track.reveal_comments)
      rep = TrackRepresentationCommentCountDecorator(counts, rep)
    if (geoblockings.isDefined)
      rep = TrackRepresentationGeoblockingsDecorator(geoblockings.get, rep)
    if (domainlockings.nonEmpty)
      rep = TrackRepresentationDomainLockingsDecorator(domainlockings, rep)
    if (!userSession.isAnonymous) {
      rep = TrackRepresentationUserFavoriteDecorator(isLiked, rep)
      rep = TrackRepresentationUserPlaybackCountDecorator(rep)
    }
    secretTokenParameter.map { secret =>
      rep = TrackRepresentationSecretTokenUriParamDecorator(rep, secret)
    }
    label.map { label =>
      rep = TrackRepresentationLabelDecorator(label, rep)
    }
    rep = TrackRepresentationQuotaDecorator(track.downloadable, downloadsPerTrack, counts.download_count, userIsOwner, rep)
    rep = TrackRepresentationWaveformUrlDecorator(waveformUrls, rep)
    rep = TrackRepresentationAttachmentsUriDecorator(track.urn, rep) // TODO: make conditional on representation type
    rep
  }

  private def fetchUserForTrack(track: Track, session: UserSession): Future[Option[User]] =
    fetchUser(track.user_urn, session)

  private def fetchLabelForTrack(track: Track, session: UserSession): Future[Option[User]] = track.label_id match {
    case Some(label_id) => fetchUser(new Urn("soundcloud", "users", label_id.toString), session)
    case None => Future.value(None)
  }

  private def fetchUserLikesTrack(track: Track, session: UserSession): Future[Boolean] =
    Option(session.getUser) match {
      case Some(user) => lieblingClient.userLikeCounts(session, List(track.urn), session.getUser)
        .map(_.liked_track_urns.contains(track.urn))
      case None => Future.value(false)
    }

  private def fetchGeoblockings(session: UserSession, urn: Urn): Future[Option[Geoblockings]] =
  // fetchTrackGeoblockings can return Some with zero geoblockings, which this method turns into None
    okidokiClient.fetchTrackGeoblockings(session, urn).map {
      case Some(geoblockings) => if (geoblockings.isEmpty) None else Some(geoblockings)
      case None => None
    }

  private def fetchUser(userUrn: Urn, session: UserSession): Future[Option[User]] =
    okidokiClient.fetchUserObjects(session, Set(userUrn)).map(_.headOption)

  private def fetchWaveformUrls(track: Track, session: UserSession): Future[Option[Seq[WaveformUrl]]] =
    mediaUrlGenClient.waveformUrls(session, track.uid)

  private def fetchDownloadsPerTrack(track: Track, session: UserSession): Future[Option[Int]] = {
    userQuotaClient.downloadsPerTrack(session, Set(track.user_urn))
      .map(_.get(track.user_urn))
      .map(_.getOrElse(None))
  }

  private def getCounts(counts: Option[StitchCounts]): StitchCounts =
    counts.getOrElse(StitchCounts(0, 0, 0, 0, 0))

  private def isTrackAccessible(session: UserSession, secretTokenInRequest: Option[String], track: Track): Future[Boolean] =
    if (!isDisabled(track)) isPrivacyAuthorized(session, secretTokenInRequest, track)
    else Future.False

  private def isPrivacyAuthorized(session: UserSession, secretTokenInRequest: Option[String], track: Track): Future[Boolean] = {
    if (track.public || track.user_urn == session.getUser) Future.True
    else isAccessGrantedViaSecretToken(secretTokenInRequest, track)
  }

  private def isAccessGrantedViaSecretToken(secretTokenInRequest: Option[String], track: Track): Future[Boolean] = {
    secretTokenInRequest match {
      case None => Future.False
      case Some(secretToken) => {
        if (secretToken.equals(track.secret_token)) Future.True
        else playlistClient.getPlaylistContainingTrackOwnedByUser(track.urn, track.user_urn)
          .map(_.exists(playlist => playlist.secretToken == secretToken && playlist.userUrn == track.user_urn))
      }
    }
  }

  private def isDisabled(track: Track): Boolean =
    track.disabled_at.isDefined
}
