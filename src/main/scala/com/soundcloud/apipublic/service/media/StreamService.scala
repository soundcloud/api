package com.soundcloud.apipublic.service.media

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.authorization.policies.{ContentPolicy, ContentRestriction}
import com.soundcloud.apipublic.client.tracks._
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService
import com.soundcloud.apipublic.service.TrackVisibilityService.TrackWithTranscodingsFieldMask
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{GetMediaStreamRequest, GetMediaStreamResponse, MediaService}

class StreamService(
    trackVisibilityService: TrackVisibilityService,
    tracksMediaService: MediaService
) {
  private val mp3MimeType = "audio/mpeg"
  private val aacMimeType = """audio/mp4; codecs="mp4a.40.2""""
  private val opusMimeType = """audio/ogg; codecs="opus""""
  private val protoProgressive = "progressive"
  private val protoHls = "hls"
  private val aac160kPreset = "aac_160k"
  private val aac96kPreset = "aac_96kk"

  def fetchUrls(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      singleStream: Boolean = false
  ): Future[Outcome[MediaStreamResponse]] = {
    trackVisibilityService
      .tracks(
        session,
        List(TrackRequest(trackUrn, secretToken)),
        TrackWithTranscodingsFieldMask,
        AccessParams.streamAccess
      )
      .flatMap(_.headOption match {
        case Some(Good(visibleTrack)) =>
          if (streamNotAllowed(visibleTrack)) {
            // For preview URLs, we need an MP3 transcoding
            visibleTrack.transcodings
              .find(transcoding => transcoding.mimeType == mp3MimeType)
              .map(transcoding => fetchPreviewUrls(session, visibleTrack, transcoding, singleStream))
              .getOrElse(Future.value(NotFound().bad))
          } else {
            // For full stream URLs, we can use MP3 or AAC
            visibleTrack.transcodings
              .find(transcoding => transcoding.mimeType == mp3MimeType || transcoding.mimeType == aacMimeType)
              .map(transcoding => fetchStreamUrls(session, visibleTrack, transcoding, singleStream))
              .getOrElse(Future.value(NotFound().bad))
          }
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
    val futureHttpStream = track.transcodings
      .find(transcoding => transcoding.mimeType == mp3MimeType && transcoding.protocols.contains(protoProgressive))
      .map(mp3 => fetchStreamUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive))
      .getOrElse(Future.value(NotFound().bad))

    if (singleStream) return futureHttpStream.map(_.map(RedirectStreamResponse))

    val futureLegacyHlsStream = track.transcodings
      .find(transcoding => transcoding.mimeType == mp3MimeType && transcoding.protocols.contains(protoHls))
      .map(mp3 => fetchStreamUrl(session, track.urn, track.secretToken, mp3.uuid, protoHls))
      .getOrElse(Future.value(NotFound().bad))
    val futureHls160kStream = track.transcodings
      .find(transcoding => transcoding.mimeType == aacMimeType && transcoding.preset == aac160kPreset)
      .map(aac => fetchStreamUrl(session, track.urn, track.secretToken, aac.uuid, protoHls))
      .getOrElse(Future.value(NotFound().bad))
    val futureHls96kStream = track.transcodings
      .find(transcoding => transcoding.mimeType == aacMimeType && transcoding.preset == aac96kPreset)
      .map(aac => fetchStreamUrl(session, track.urn, track.secretToken, aac.uuid, protoHls))
      .getOrElse(Future.value(NotFound().bad))
    val futureOpusStream = track.transcodings
      .find(_.mimeType == opusMimeType)
      .map(opus => fetchStreamUrl(session, track.urn, track.secretToken, opus.uuid, protoHls))
      .getOrElse(Future.value(NotFound().bad))
    val futureMp3Preview = track.transcodings
      .find(transcoding => transcoding.mimeType == mp3MimeType)
      .map(mp3 => fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoProgressive))
      .getOrElse(Future.value(NotFound().bad))

    Future
      .join(
        futureHttpStream,
        futureHls160kStream,
        futureHls96kStream,
        futureLegacyHlsStream,
        futureOpusStream,
        futureMp3Preview
      )
      .map {
        case (rHttp, rHls160k, rHls96k, rLegacyHls, rOpus, rPreview) =>
          val urls = MediaStreamUrls(
            rHttp.toOption,
            rLegacyHls.toOption,
            rHls96k.toOption,
            rHls160k.toOption,
            rOpus.toOption,
            rPreview.toOption
          )

          val errors: List[ApplicationError] =
            List(rHttp, rHls160k, rHls96k, rLegacyHls, rOpus, rPreview).collect { case Bad(e) => e }

          def allNone(msu: MediaStreamUrls): Boolean =
            List(
              msu.httpMp3,
              msu.hlsMp3,
              msu.hlsAac96k,
              msu.hlsAac160k,
              msu.hlsOpus,
              msu.httpPreviewMp3
            ).forall(_.isEmpty)

          if (allNone(urls)) {
            // return the first error
            errors.headOption
              .map(Bad(_))
              .get
          } else {
            // Partial success (some options are present). Return Good(urls).
            Good(urls)
          }
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

    val futureLegacyHlsStream = fetchPreviewUrl(session, track.urn, track.secretToken, mp3.uuid, protoHls)

    Future
      .join(futureHttpStream, futureLegacyHlsStream)
      .map {
        case (Good(http), Good(hlsMp3)) =>
          MediaStreamUrls(Some(http), Some(hlsMp3)).good
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
