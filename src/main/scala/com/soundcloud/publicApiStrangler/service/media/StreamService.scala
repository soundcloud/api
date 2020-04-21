package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, ContentRestriction}
import com.soundcloud.publicApiStrangler.client.tracks._
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService
import com.twitter.util.Future

class StreamService(
    trackVisibilityService: TrackVisibilityService,
    tracksClient: TracksClient
) {
  private val mp3MimeType = "audio/mpeg"
  private val opusMimeType = """audio/ogg; codecs="opus""""
  private val allowedMimeTypes = Set(mp3MimeType, opusMimeType)
  private val protoProgressive = "progressive"
  private val protoHls = "hls"

  def fetchMultiple(session: UserSession, trackUrn: Urn, secretToken: Option[String]): Future[MediaStreamResponse] =
    fetch(session, trackUrn, secretToken, fetchTranscodingUrls)

  def fetchSingle(session: UserSession, trackUrn: Urn, secretToken: Option[String]): Future[MediaStreamResponse] =
    fetch(session, trackUrn, secretToken, fetchTranscodingUrl)

  private type Fetch = (UserSession, VisibleTrack) => Future[MediaStreamResponse]

  private def fetch(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      fetcher: Fetch
  ): Future[MediaStreamResponse] = {
    trackVisibilityService
      .tracks(session, List(TrackRequest(trackUrn, secretToken)))
      .map(_.headOption)
      .flatMap {
        case Some(track) => fetcher(session, track)
        case None => Future.value(MediaStreamNotFoundError)
      }
  }

  private def extractTranscodings(session: UserSession, track: VisibleTrack): Map[String, Transcoding] = {
    track.transcodings
      .groupBy(_.mimeType)
      .filter { case (mimeType, _) => allowedMimeTypes.contains(mimeType) }
      .mapValues(_.head)
  }

  private def fetchTranscodingUrl(
      session: UserSession,
      track: VisibleTrack
  ): Future[MediaStreamResponse] = {
    extractTranscodings(session, track).get(mp3MimeType) match {
      case Some(mp3) =>
        val streamRequest = StreamRequest(track.urn, track.secretToken, mp3.uuid, protoProgressive)
        val streamResponse =
          // Some labels disallow progressive streams. The best thing we can do in this case is to downgrade to a snippet.
          if (track.authorization.policy == ContentPolicy.SNIP || track.authorization.contentRestrictions.contains(
              ContentRestriction.NO_PROGRESSIVE_DOWNLOAD
            ))
            tracksClient.previewUrl(session, streamRequest)
          else
            tracksClient.streamUrl(session, streamRequest)
        streamResponse.map {
          case StreamUrlResponse(url, _) => MediaStreamUrl(url)
          case _ => MediaStreamNotFoundError
        }
      case None => Future.value(MediaStreamNotFoundError)
    }
  }

  private def fetchTranscodingUrls(
      session: UserSession,
      track: VisibleTrack
  ): Future[MediaStreamResponse] = {
    val transcodings = extractTranscodings(session, track)
    transcodings.get(mp3MimeType) match {
      case Some(mp3) =>
        // Some labels disallow progressive streams. The best thing we can do in this case is to downgrade to a snippet.
        if (track.authorization.policy == ContentPolicy.SNIP || track.authorization.contentRestrictions.contains(
            ContentRestriction.NO_PROGRESSIVE_DOWNLOAD
          ))
          fetchPreviewUrls(session, track, mp3)
        else
          fetchStreamUrls(session, track, mp3, transcodings.get(opusMimeType))
      case None => Future.value(MediaStreamNotFoundError)
    }
  }

  private def fetchStreamUrls(
      session: UserSession,
      track: VisibleTrack,
      mp3: Transcoding,
      maybeOpus: Option[Transcoding]
  ): Future[MediaStreamResponse] = {
    for {
      (maybeHttpStream, maybeHlsStream, maybeMp3Preview, maybeOpusStream) <- Future.join(
        tracksClient.streamUrl(session, StreamRequest(track.urn, track.secretToken, mp3.uuid, protoProgressive)),
        tracksClient.streamUrl(session, StreamRequest(track.urn, track.secretToken, mp3.uuid, protoHls)),
        tracksClient.previewUrl(session, StreamRequest(track.urn, track.secretToken, mp3.uuid, protoProgressive)),
        maybeOpus match {
          case Some(opus) =>
            tracksClient.streamUrl(session, StreamRequest(track.urn, track.secretToken, opus.uuid, protoHls))
          case None => Future.None
        }
      )
    } yield (maybeHttpStream, maybeHlsStream, maybeMp3Preview, maybeOpusStream) match {
      case (
          StreamUrlResponse(httpStream, _),
          StreamUrlResponse(hlsStream, _),
          StreamUrlResponse(preview, _),
          opusStreamResult
          ) =>
        opusStreamResult match {
          case StreamUrlResponse(opusUrl, _) =>
            MediaStreamUrls(httpStream, hlsStream, Some(opusUrl), preview)
          case _ =>
            MediaStreamUrls(httpStream, hlsStream, None, preview)
        }
      case _ => MediaStreamNotFoundError
    }
  }

  private def fetchPreviewUrls(
      session: UserSession,
      track: VisibleTrack,
      mp3: Transcoding
  ): Future[MediaStreamResponse] = {
    for {
      (maybeHttp, maybeHls) <- Future.join(
        tracksClient.previewUrl(session, StreamRequest(track.urn, track.secretToken, mp3.uuid, protoProgressive)),
        tracksClient.previewUrl(session, StreamRequest(track.urn, track.secretToken, mp3.uuid, protoHls))
      )
    } yield (maybeHttp, maybeHls) match {
      case (StreamUrlResponse(http, _), StreamUrlResponse(hls, _)) => PreviewUrls(http, hls)
      case _ => MediaStreamNotFoundError
    }
  }
}
