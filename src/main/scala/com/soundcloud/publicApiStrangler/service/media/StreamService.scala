package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.ContentPolicy
import com.soundcloud.publicApiStrangler.client.media.{MediaServiceClient, Transcoding}
import com.soundcloud.publicApiStrangler.client.tracks.{TracksClient, VisibleTrack}
import com.twitter.util.Future


class StreamService(tracksClient: TracksClient,
                    mediaServiceClient: MediaServiceClient) {

  private val mp3MimeType = "audio/mpeg"
  private val opusMimeType = """audio/ogg; codecs="opus""""
  private val allowedMimeTypes = Set(mp3MimeType, opusMimeType)
  private val protoProgressive = "progressive"
  private val protoHls = "hls"

  def fetchMultiple(session: UserSession, trackUrn: Urn, secretToken: Option[String]): Future[StreamResponse] =
    fetch(session, trackUrn, secretToken, fetchTranscodingUrls)

  def fetchSingle(session: UserSession, trackUrn: Urn, secretToken: Option[String]): Future[StreamResponse] =
    fetch(session, trackUrn, secretToken, fetchTranscodingUrl)

  private type Fetch = (UserSession, Map[String, Transcoding], ContentPolicy) => Future[StreamResponse]

  private def fetch(session: UserSession, trackUrn: Urn, secretToken: Option[String], fetcher: Fetch): Future[StreamResponse] = {
    tracksClient.visibleTrack(session, trackUrn, secretToken).flatMap {
      case Some(track) if streamingAllowed(track) =>
        track.uid match {
          case Some(uid) => fetchTranscodings(session, uid).flatMap { transcodings => fetcher(session, transcodings, track.authorization.policy) }
          case None => Future.value(StreamNotFoundError)
        }
      case _ => Future.value(StreamNotFoundError)
    }
  }

  private def streamingAllowed(track: VisibleTrack): Boolean = {
    track.apiStreamable.getOrElse(false) &&
      track.authorization.policy != ContentPolicy.BLOCK
  }

  private def fetchTranscodings(session: UserSession, trackUid: String): Future[Map[String, Transcoding]] = {
    mediaServiceClient.fetchTranscodings(session, trackUid).map { transcodings =>
      transcodings
        .groupBy(_.mime_type)
        .filter { case (mimeType, _) => allowedMimeTypes.contains(mimeType) }
        .mapValues(_.head)
    }
  }

  private def fetchTranscodingUrl(session: UserSession, transcodings: Map[String, Transcoding], policy: ContentPolicy): Future[StreamResponse] = {
    transcodings.get(mp3MimeType) match {
      case Some(mp3) =>
        (if (policy == ContentPolicy.SNIP)
          mediaServiceClient.fetchPreviewUrl(session, mp3.uuid, protoProgressive)
        else
          mediaServiceClient.fetchStreamUrl(session, mp3.uuid, protoProgressive)
        ).map {
          case Some(url) => StreamUrl(url)
          case None => StreamNotFoundError
        }
      case None => Future.value(StreamNotFoundError)
    }
  }

  private def fetchTranscodingUrls(session: UserSession, transcodings: Map[String, Transcoding], policy: ContentPolicy): Future[StreamResponse] = {
    transcodings.get(mp3MimeType) match {
      case Some(mp3) =>
        if (policy == ContentPolicy.SNIP)
          fetchPreviewUrls(session, mp3)
        else
          fetchStreamUrls(session, mp3, transcodings.get(opusMimeType))
      case None => Future.value(StreamNotFoundError)
    }
  }

  private def fetchStreamUrls(session: UserSession, mp3: Transcoding, maybeOpus: Option[Transcoding]): Future[StreamResponse] = {
    for {
      (maybeHttpStream, maybeHlsStream, maybeMp3Preview, maybeOpusStream) <- Future.join(
        mediaServiceClient.fetchStreamUrl(session, mp3.uuid, protoProgressive),
        mediaServiceClient.fetchStreamUrl(session, mp3.uuid, protoHls),
        mediaServiceClient.fetchPreviewUrl(session, mp3.uuid, protoProgressive),
        maybeOpus match {
          case Some(opus) => mediaServiceClient.fetchStreamUrl(session, opus.uuid, protoHls)
          case None => Future.None
        }
      )
    } yield (maybeHttpStream, maybeHlsStream, maybeMp3Preview) match {
      case (Some(httpStream), Some(hlsStream), Some(preview)) => StreamUrls(httpStream, hlsStream, maybeOpusStream, preview)
      case _ => StreamNotFoundError
    }
  }

  private def fetchPreviewUrls(session: UserSession, mp3: Transcoding): Future[StreamResponse] = {
    for {
      (maybeHttp, maybeHls) <- Future.join(
        mediaServiceClient.fetchPreviewUrl(session, mp3.uuid, protoProgressive),
        mediaServiceClient.fetchPreviewUrl(session, mp3.uuid, protoHls),
      )
    } yield (maybeHttp, maybeHls) match {
      case (Some(http), Some(hls)) => PreviewUrls(http, hls)
      case _ => StreamNotFoundError
    }
  }
}
