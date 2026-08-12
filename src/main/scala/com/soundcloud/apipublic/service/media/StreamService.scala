package com.soundcloud.apipublic.service.media

import com.soundcloud.apipublic.authorization.policies.{Access, ContentPolicy, ContentRestriction}
import com.soundcloud.apipublic.client.tracks._
import com.soundcloud.apipublic.handler.PlayParams
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService
import com.soundcloud.apipublic.service.TrackVisibilityService.TrackWithTranscodingsFieldMask
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{GetMediaStreamRequest, GetMediaStreamResponse, MediaService}

class StreamService(
    trackVisibilityService: TrackVisibilityService,
    tracksMediaService: MediaService,
    baseUrl: String
) {

  private final val PROTOCOL_HLS: String = "hls"
  private final val PROTOCOL_PREVIEW_HLS: String = "hls-preview"
  private final val PROTOCOL_PROGRESSIVE: String = "http"
  private final val PROTOCOL_PREVIEW_PROGRESSIVE: String = "http-preview"
  private val mp3MimeType = "audio/mpeg"
  private val aacMimeType = """audio/mp4; codecs="mp4a.40.2""""
  private val protoProgressive = "progressive"
  private val protoHls = "hls"
  private val aac160kPreset = "aac_160k"
  private val aac96kPreset = "aac_96kk"

  def fetchLegacyProgressivePreviewTranscodingUrl(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String]
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
          visibleTrack.transcodings
          // For preview URLs, we need an MP3 transcoding
            .find(transcoding => transcoding.mimeType == mp3MimeType)
            .map(transcoding => {
              fetchProgressivePreviewUrl(session, trackUrn, secretToken, transcoding.uuid)
            }.map(_.map(RedirectStreamResponse(_))))
            .getOrElse(Future.value(NotFound().bad))
        case Some(Bad(err)) => Future.value(err.bad)
        case _ => Future.value(NotFound().bad)
      })
  }
  // Some labels disallow progressive streams. The best thing we can do in this case is to downgrade to a snippet.
  private def streamNotAllowed(track: VisibleTrack): Boolean =
    track.authorization.policy == ContentPolicy.SNIP ||
      track.authorization.contentRestrictions.contains(ContentRestriction.NO_PROGRESSIVE_DOWNLOAD)

  /** Full (non-preview) stream URLs must not be exposed when the track resolves to Preview access. */
  private def usePreviewOnlyStreamEndpoints(track: VisibleTrack): Boolean =
    streamNotAllowed(track) || track.access.contains(Access.Preview)

  /**
    * Supply-chain catalogue tracks keep only `preview_mp3_128_url` on GET /streams — not full AAC HLS fields (those
    * suggest full stream tiers). Other Preview access (e.g. high tier) still receives the multi-field response with
    * snippet endpoints on each URL field; SNIP behaves the same way.
    */
  private def listResponsesWithOnlyPartialPreviewField(track: VisibleTrack): Boolean =
    track.supplyChainStatus.exists(_ == TrackVisibilityService.SUPPLY_CHAIN_STATUS_SUPPLY_CHAIN)

  def fetchStreamUrl(session: UserSession, params: PlayParams): Future[Outcome[RedirectStreamResponse]] = {
    trackVisibilityService
      .tracks(
        session,
        List(TrackRequest(params.trackUrn, params.secretToken)),
        TrackWithTranscodingsFieldMask,
        AccessParams.streamAccess
      )
      .flatMap(_.headOption match {
        case Some(Good(visibleTrack)) =>
          val fetched = params.protocol match {
            case PROTOCOL_PREVIEW_HLS =>
              fetchHlsPreviewUrl(session, params.trackUrn, params.secretToken, params.transcoding)
            case PROTOCOL_PREVIEW_PROGRESSIVE =>
              fetchProgressivePreviewUrl(session, params.trackUrn, params.secretToken, params.transcoding)
            case PROTOCOL_PROGRESSIVE =>
              if (usePreviewOnlyStreamEndpoints(visibleTrack))
                fetchProgressivePreviewUrl(session, params.trackUrn, params.secretToken, params.transcoding)
              else
                fetchProgressiveUrl(session, params.trackUrn, params.secretToken, params.transcoding)
            case PROTOCOL_HLS =>
              if (usePreviewOnlyStreamEndpoints(visibleTrack))
                fetchHlsPreviewUrl(session, params.trackUrn, params.secretToken, params.transcoding)
              else
                fetchHlsUrl(session, params.trackUrn, params.secretToken, params.transcoding)
          }
          fetched.map(_.map(RedirectStreamResponse(_)))
        case Some(Bad(err)) => Future.value(err.bad)
        case _ => Future.value(NotFound().bad)
      })
  }

  def fetchTranscodingUrls(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String]
  ): Future[Outcome[MediaStreamUrls]] = {
    trackVisibilityService
      .tracks(
        session,
        List(TrackRequest(trackUrn, secretToken)),
        TrackWithTranscodingsFieldMask,
        AccessParams.streamAccess
      )
      .flatMap(_.headOption match {
        case Some(Good(visibleTrack)) => fetchTranscodingUrls(visibleTrack).map(Good(_))
        case Some(Bad(err)) => Future.value(err.bad)
        case _ => Future.value(NotFound().bad)
      })

  }

  private def fetchTranscodingUrls(track: VisibleTrack): Future[MediaStreamUrls] = {
    val hlsProtocol = if (usePreviewOnlyStreamEndpoints(track)) PROTOCOL_PREVIEW_HLS else PROTOCOL_HLS
    val secretTokenParam = if (track.secretToken.isDefined) s"?secret_token=${track.secretToken.get}" else ""

    def buildStreamUrl(transcoding: Transcoding, protocol: String): String =
      s"$baseUrl/tracks/${track.urn}/streams/${transcoding.uuid}/$protocol$secretTokenParam"

    def findTranscoding(
        mimeType: String,
        protocol: Option[String] = None,
        preset: Option[String] = None
    ): Option[String] =
      track.transcodings
        .find(t =>
          t.mimeType == mimeType &&
            protocol.forall(t.protocols.contains) &&
            preset.forall(t.preset == _)
        )
        .map(t => buildStreamUrl(t, hlsProtocol))

    val legacyHlsStream = findTranscoding(mp3MimeType, Some(protoHls))
    val hls160kStream = findTranscoding(aacMimeType, preset = Some(aac160kPreset))
    val hls96kStream = findTranscoding(aacMimeType, preset = Some(aac96kPreset))
    val mp3Preview = track.transcodings
      .find(_.mimeType == mp3MimeType)
      .map(mp3 => buildStreamUrl(mp3, PROTOCOL_PREVIEW_PROGRESSIVE))

    val urls =
      if (listResponsesWithOnlyPartialPreviewField(track))
        MediaStreamUrls(
          hlsMp3 = None,
          hlsAac96k = None,
          hlsAac160k = None,
          httpPreviewMp3 = mp3Preview
        )
      else
        MediaStreamUrls(
          legacyHlsStream,
          hls96kStream,
          hls160kStream,
          httpPreviewMp3 = mp3Preview
        )

    Future.value(urls)
  }

  private def fetchHlsUrl: (UserSession, Urn, Option[String], String) => Future[Outcome[String]] =
    (session, urn, secretToken, transcodingId) =>
      fetch(tracksMediaService.getMediaStream)(session, urn, secretToken, transcodingId, protoHls)

  private def fetchHlsPreviewUrl: (UserSession, Urn, Option[String], String) => Future[Outcome[String]] =
    (session, urn, secretToken, transcodingId) =>
      fetch(tracksMediaService.getMediaPreview)(session, urn, secretToken, transcodingId, protoHls)

  private def fetchProgressivePreviewUrl: (UserSession, Urn, Option[String], String) => Future[Outcome[String]] =
    (session, urn, secretToken, transcodingId) =>
      fetch(tracksMediaService.getMediaPreview)(session, urn, secretToken, transcodingId, protoProgressive)

  private def fetchProgressiveUrl: (UserSession, Urn, Option[String], String) => Future[Outcome[String]] =
    (session, urn, secretToken, transcodingId) =>
      fetch(tracksMediaService.getMediaStream)(session, urn, secretToken, transcodingId, protoProgressive)

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
