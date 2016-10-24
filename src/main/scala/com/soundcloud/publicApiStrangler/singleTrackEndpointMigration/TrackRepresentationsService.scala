package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.service.client.LieblingClient
import com.soundcloud.service.response.representation._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, NonFatal}

class TrackRepresentationsService(
  trackmetadataClient: TrackmetadataClient,
  okidokiClient: RichOkidokiClient,
  pubmeseClient: PubmeseClient,
  stitchClient: StitchClient,
  lieblingClient: LieblingClient,
  mediaUrlGenClient: MediaServiceUrlGenClient,
  userQuotaClient: UserQuotaClient) {

  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass.getName)
  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
  private val serviceUnavailableErrorString = """{"errors":[{"error_message":"503 - Service Unavailable"}]}"""

  def track(session: UserSession, urn: Urn, secretToken: Option[String], callback: Option[String]): Future[Response] = {
    val isrcF = pubmeseClient.isrcForTrack(session, urn).handle { case NonFatal(ex) => None }
    val geoblockingsF = fetchGeoblockings(session, urn).handle { case NonFatal(ex) => None }
    val domainlockingsF = okidokiClient.fetchTrackDomainLockings(session, urn).handle { case NonFatal(ex) => Seq() }
    val audioF = okidokiClient.fetchTrackAudioMetadata(session, urn)
    implicit val trackRepresentationWrites = TrackRepresentation.writes

    trackmetadataClient.track(session, urn).flatMap {
      case Some(track) if isTrackAccessible(session, secretToken, track) =>
        val userF = fetchUserForTrack(track, session)
        val labelF = fetchLabelForTrack(track, session)
        val isLikedF = fetchUserLikesTrack(track, session)
        val waveformUrlsF = fetchWaveformUrls(track, session)
        val downloadsPerTrackF = fetchDownloadsPerTrack(track, session)
        val countsF = userF.flatMap {
          case Some(user) => stitchClient.countsForTrack(session, urn, user.urn).map(Some(_)).liftToTry.map(_.getOrElse(None))
          case None => Future.value(None)
        }

        Future.join(isrcF, userF, countsF, labelF, geoblockingsF, domainlockingsF, audioF, isLikedF, waveformUrlsF, downloadsPerTrackF).map {
          case (isrc, Some(user), counts, label, geoblockings, domainlockings, Some(audio), isLiked, waveformUrls, downloadsPerTrack) =>
            val rep = buildTrackRepresentationLike(
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
              secretTokenParameter = secretToken,
              downloadsPerTrack = downloadsPerTrack
            )

            generateResponse(Status.Ok, jsonpWrapper(callback, Json.stringify(rep)))
          case _ =>
            generateResponse(Status.NotFound, jsonpWrapper(callback, notFoundErrorString))
        }
        .handle {
          case NonFatal(ex) =>
            logger.error("Error while generating legacy response", ex)
            generateResponse(Status.ServiceUnavailable, jsonpWrapper(callback, serviceUnavailableErrorString))
        }
      case _ =>
        Future.value(generateResponse(Status.NotFound, jsonpWrapper(callback, notFoundErrorString)))
    }
  }

  /**
    * This JsonpWrapper logic should go to filter,
    * but should be applied only to the migrated endpoitns.
    */
  private def jsonpWrapper(callback: Option[String], contentString: String): String =
    callback.map(cb => s"/**/$cb($contentString);").getOrElse(contentString)

  private def generateResponse(status: Status, content: String): Response = {
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(status)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    res
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

    var rep: TrackRepresentationLike = basicTrackRep
    // TODO Consider an "owning user" decorator
    if (track.user_urn == userSession.getUser)
      rep = TrackRepresentationSecretTokenDecorator(track, rep)
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
    val userIsOwner = track.user_urn == userSession.getUser
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

  private def fetchWaveformUrls(track: Track, session: UserSession): Future[Seq[WaveformUrl]] =
    mediaUrlGenClient.waveformUrls(session, track.uid)

  private def fetchDownloadsPerTrack(track: Track, session: UserSession): Future[Option[Int]] =
    userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)).map(_.get(track.user_urn))

  private def getCounts(counts: Option[StitchCounts]): StitchCounts =
    counts.getOrElse(StitchCounts(0, 0, 0, 0))

  private def isTrackAccessible(session: UserSession, secretToken: Option[String], track: Track): Boolean =
    isPrivacyAuthorized(session, secretToken, track) && !isDisabled(track)

  private def isPrivacyAuthorized(session: UserSession, secretToken: Option[String], track: Track): Boolean =
    track.public || track.user_urn == session.getUser || secretToken.contains(track.secret_token)

  private def isDisabled(track: Track): Boolean =
    track.disabled_at.isDefined
}
