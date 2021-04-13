package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, ContentRestriction}
import com.soundcloud.publicApiStrangler.client.tracks._
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

  def fetchMultiple(session: UserSession, trackUrn: Urn, secretToken: Option[String]): Future[MediaStreamResponse] =
    fetch(session, trackUrn, secretToken, singleStream = false)

  def fetchSingle(session: UserSession, trackUrn: Urn, secretToken: Option[String]): Future[MediaStreamResponse] =
    fetch(session, trackUrn, secretToken, singleStream = true)

  private def fetch(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      singleStream: Boolean
  ): Future[MediaStreamResponse] = {
    trackVisibilityService
      .tracks(session, List(TrackRequest(trackUrn, secretToken)))
      .map(_.headOption)
      .flatMap(visibleTrack =>
        visibleTrack
          .flatMap(track => {
            track.transcodings
              .find(_.mimeType == mp3MimeType)
              .map(mp3 => {
                if (streamNotAllowed(track)) fetchPreviewUrls(session, track, mp3, singleStream)
                else fetchStreamUrls(session, track, mp3, singleStream)
              })
          })
          .getOrElse(Future.value(MediaStreamNotFoundError))
      )
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
  ): Future[MediaStreamResponse] = {
    val futureHttpStream = fetchStreamUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive)

    if (singleStream) {
      return futureHttpStream.map {
        case Some(http) => RedirectStreamResponse(http)
        case None => MediaStreamNotFoundError
      }
    }

    val futureHlsStream = fetchStreamUrl(session, track.urn, track.secretToken, mp3.uuid, protoHls)
    val futureOpusStream = track.transcodings
      .find(_.mimeType == opusMimeType)
      .map(opus => fetchStreamUrl(session, track.urn, track.secretToken, opus.uuid, protoHls))
      .getOrElse(Future.None)
    val futureMp3Preview = fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive)

    Future
      .join(futureHttpStream, futureHlsStream, futureOpusStream, futureMp3Preview)
      .map {
        case (Some(httpStream), Some(hlsStream), opus, preview) =>
          MediaStreamUrls(httpStream, hlsStream, opus, preview)
        case _ => MediaStreamNotFoundError
      }
  }

  private def fetchPreviewUrls(
      session: UserSession,
      track: VisibleTrack,
      mp3: Transcoding,
      singleStream: Boolean
  ): Future[MediaStreamResponse] = {
    val futureHttpStream = fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive)
    val futureHlsStream =
      if (singleStream) Future.None else fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoHls)

    Future
      .join(futureHttpStream, futureHlsStream)
      .map {
        case (Some(http), Some(hls)) => MediaStreamUrls(http, hls)
        case (Some(http), _) if singleStream => RedirectStreamResponse(http)
        case _ => MediaStreamNotFoundError
      }
  }

  private def fetchPreviewUrl: (UserSession, Urn, Option[String], String, String) => Future[Option[String]] =
    fetchUrl(tracksMediaService.getMediaPreview)

  private def fetchStreamUrl: (UserSession, Urn, Option[String], String, String) => Future[Option[String]] =
    fetchUrl(tracksMediaService.getMediaStream)

  private def fetchUrl(fetchAction: GetMediaStreamRequest => Future[GetMediaStreamResponse])(
      session: UserSession,
      urn: Urn,
      secretToken: Option[String],
      transcodingId: String,
      protocol: String
  ): Future[Option[String]] = {
    val protoUserSession = session.asProtoSession

    val request = GetMediaStreamRequest(
      userSession = Some(protoUserSession),
      urn = urn.toString,
      secretToken = secretToken,
      transcodingId = transcodingId,
      protocol = protocol
    )

    fetchAction(request)
      .map(res => Some(res.url))
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => None
        case TwinagleException(ErrorCode.Unauthenticated, _, _, _) => None
      }
  }
}
