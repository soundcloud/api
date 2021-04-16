package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, ContentRestriction}
import com.soundcloud.publicApiStrangler.client.tracks._
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{GetMediaStreamRequest, GetMediaStreamResponse, MediaService}

class StreamService(
    trackVisibilityService: TrackVisibilityService,
    tracksMediaService: MediaService
) {
  private val mp3MimeType = "audio/mpeg"
  private val opusMimeType = """audio/ogg; codecs="opus""""
  private val protoProgressive = "progressive"
  private val protoHls = "hls"

  def fetchUrls(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      singleStream: Boolean = false
  ): Future[Outcome[MediaStreamResponse]] = {
    trackVisibilityService
      .tracks(session, List(TrackRequest(trackUrn, secretToken)), AccessParams.streamAccess)
      .flatMap(_.headOption match {
        case Some(Good(visibleTrack)) =>
          visibleTrack.transcodings
            .find(_.mimeType == mp3MimeType)
            .map(mp3 => {
              if (streamNotAllowed(visibleTrack)) fetchPreviewUrls(session, visibleTrack, mp3, singleStream)
              else fetchStreamUrls(session, visibleTrack, mp3, singleStream)
            })
            .getOrElse(Future.value(NotFound().bad))
        case Some(Bad(err)) => Future.value(err.bad)
        case _ => Future.value(NotFound().bad)
      })
  }

  // Some labels disallow progressive streams. The best thing we can do in this case is to downgrade to a snippet.
  private def streamNotAllowed(track: VisibleTrack): Boolean =
    track.authorization.policy == ContentPolicy.SNIP ||
      track.authorization.contentRestrictions.contains(ContentRestriction.NO_PROGRESSIVE_DOWNLOAD)

  private def fetchStreamUrls(
      session: UserSession,
      track: VisibleTrack,
      mp3: Transcoding,
      singleStream: Boolean
  ): Future[Outcome[MediaStreamResponse]] = {
    val futureHttpStream = fetchStreamUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive)

    if (singleStream) return futureHttpStream.map(_.map(RedirectStreamResponse))

    val futureHlsStream = fetchStreamUrl(session, track.urn, track.secretToken, mp3.uuid, protoHls)
    val futureOpusStream = track.transcodings
      .find(_.mimeType == opusMimeType)
      .map(opus => fetchStreamUrl(session, track.urn, track.secretToken, opus.uuid, protoHls))
      .getOrElse(Future.value(NotFound().bad))
    val futureMp3Preview = fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive)

    Future
      .join(futureHttpStream, futureHlsStream, futureOpusStream, futureMp3Preview)
      .map {
        case (Good(http), Good(hls), opus, preview) => MediaStreamUrls(http, hls, opus.toOption, preview.toOption).good
        case (Bad(err), _, _, _) => err.bad
        case (_, Bad(err), _, _) => err.bad
      }
  }

  private def fetchPreviewUrls(
      session: UserSession,
      track: VisibleTrack,
      mp3: Transcoding,
      singleStream: Boolean
  ): Future[Outcome[MediaStreamResponse]] = {
    val futureHttpStream = fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive)

    if (singleStream) return futureHttpStream.map(_.map(RedirectStreamResponse))

    val futureHlsStream = fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoHls)

    Future
      .join(futureHttpStream, futureHlsStream)
      .map {
        case (Good(http), Good(hls)) => MediaStreamUrls(http, hls).good
        case (Bad(err), _) => err.bad
        case (_, Bad(err)) => err.bad
      }
  }

  private def fetchPreviewUrl: (UserSession, Urn, Option[String], String, String) => Future[Outcome[String]] =
    fetch(tracksMediaService.getMediaPreview)

  private def fetchStreamUrl: (UserSession, Urn, Option[String], String, String) => Future[Outcome[String]] =
    fetch(tracksMediaService.getMediaStream)

  private def fetch(fetchAction: GetMediaStreamRequest => Future[GetMediaStreamResponse])(
      session: UserSession,
      urn: Urn,
      secretToken: Option[String],
      transcodingId: String,
      protocol: String
  ): Future[Outcome[String]] = {
    val protoUserSession = session.asProtoSession

    val request = GetMediaStreamRequest(
      userSession = Some(protoUserSession),
      urn = urn.toString,
      secretToken = secretToken,
      transcodingId = transcodingId,
      protocol = protocol
    )

    fetchAction(request)
      .map(res => res.url.good)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound().bad
        case TwinagleException(ErrorCode.Unauthenticated, _, _, _) => NotAuthorized().bad
      }
  }
}
