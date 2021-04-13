package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.authorization.policies.{
  ContentAuthorization,
  ContentPolicy,
  ContentRestriction
}
import com.soundcloud.publicApiStrangler.client.tracks.{ContentAuthorizationBuilder, _}
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
import proto.soundcloud.tracks.api.{GetMediaStreamRequest, GetMediaStreamResponse, MediaService}

class StreamServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackVisibilityService = mock[TrackVisibilityService]
    val tracksMediaTwirpClient = mock[MediaService]

    val service = new StreamService(trackVisibilityService, tracksMediaTwirpClient)
    val session = new UserSessionBuilder().build()

    val track = mock[VisibleTrack]
    val trackUid = "some-uid"
    val trackUrn = Urn("soundcloud", "tracks", "2")
    val userUrn = Urn("soundcloud", "users", "42")

    val secretToken = Some("secret")
    lazy val maybeStreamable = Some(true)

    lazy val uid: Option[String] = Some(trackUid)
    lazy val disabledAt: Option[LocalDateTime] = None

    track.uid returns uid
    track.apiStreamable returns maybeStreamable
    track.userUrn returns userUrn
    track.disabledAt returns disabledAt

    val streamRequest = mock[StreamRequest]

    lazy val policy: ContentPolicy = ContentPolicy.ALLOW
    lazy val contentAuth = new ContentAuthorizationBuilder().setPolicy(policy).build

    lazy val transcodings = List(mp3Transcoding, opusTranscoding)
    lazy val mp3Transcoding =
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive"), None, "sq", 180000, None)
    lazy val opusTranscoding =
      Transcoding("opus-uuid", "preset", """audio/ogg; codecs="opus"""", List("progressive"), None, "sq", 180000, None)

    lazy val visibleTrack = new VisibleTrackBuilder()
      .setUrn(trackUrn)
      .setUid(uid)
      .setPublic(true)
      .setSecretToken(Some("super-secret"))
      .setAuthorization(contentAuth)
      .setTranscodings(transcodings)
      .build

    val trackRequest = TrackRequest(trackUrn, secretToken)
    val trackClientResponse = mock[TrackRequest]
    lazy val tracks = List(visibleTrack.good)

    lazy val streamUrlTwirpResponse = GetMediaStreamResponse("http://stream", "audio/mpeg")
    lazy val streamPreviewUrlTwirpResponse = GetMediaStreamResponse("http://snippet", "audio/mpeg")

    tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.value(streamUrlTwirpResponse)

    tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.value(
      streamPreviewUrlTwirpResponse
    )

    trackVisibilityService.tracks(session, List(trackRequest)) returns Future.value(tracks)
  }

  "error when no track is found" in new Context {
    override lazy val tracks = List[Outcome[VisibleTrack]]()
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== NotFound().bad
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== NotFound().bad
  }

  "error when no MP3 transodings are returned" in new Context {
    override lazy val transcodings = List(opusTranscoding)
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== NotFound().bad
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== NotFound().bad
  }

  "downgrades to snippet when MP3 transodings are returned but progressive streaming restricted" in new Context {
    override lazy val tracks = List(
      visibleTrack
        .copy(authorization = new ContentAuthorization(
          visibleTrack.urn,
          visibleTrack.authorization.getPolicy,
          visibleTrack.authorization.getReason,
          Set[ContentRestriction](ContentRestriction.NO_PROGRESSIVE_DOWNLOAD),
          visibleTrack.authorization.getMonetizationModel
        )
        )
        .good
    )

    override lazy val transcodings = List(mp3Transcoding)
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== RedirectStreamResponse("http://snippet").good
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== MediaStreamUrls(
      "http://snippet",
      "http://snippet"
    ).good
  }

  "#fetchSingle" >> {
    "when track is streamable and policy is not BLOCK" >> {
      "Map NotFound response from Tracks to MediaStreamNotFoundError" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.NotFound, "stream not found")
        )

        tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.NotFound, "stream not found")
        )

        Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== NotFound().bad
      }

      "Map Unauthenticated response from Tracks to MediaStreamNotFoundError" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
        )

        tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
        )

        Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== NotFound().bad
      }

      "returns an MP3 stream url with rollout" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.value(streamUrlTwirpResponse)

        val result = Await.result(service.fetchSingle(session, trackUrn, secretToken))

        result ==== RedirectStreamResponse("http://stream").good
      }

      "returns an MP3 snippet url if policy is SNIP" in new Context {
        override lazy val policy = ContentPolicy.SNIP
        val result = Await.result(service.fetchSingle(session, trackUrn, secretToken))
        result ==== RedirectStreamResponse("http://snippet").good
      }
    }
  }

  "#fetchMultiple" >> {
    "Map NotFound response from Tracks to bad outcome" in new Context {
      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "stream not found")
      )

      tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "stream not found")
      )

      Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== NotFound().bad
    }

    "Map Unauthenticated response from Tracks to MediaStreamNotFoundError" in new Context {
      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
      )

      tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
      )

      Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== NotFound().bad
    }

    "returns multiple stream urls and one snippet url" in new Context {
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val c = "http://stream/opus/hls"
      val d = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(c, """audio/ogg; codecs="opus"""")))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(a, b, Some(c), Some(d)).good
    }

    "returns multiple snippet urls if policy is SNIP" in new Context {
      override lazy val policy = ContentPolicy.SNIP
      val a = "http://stream/mp3/progressive/preview"
      val b = "http://stream/mp3/hls/preview"

      tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns (
        Future.value(
          GetMediaStreamResponse(a, "audio/mpeg")
        ),
        Future.value(GetMediaStreamResponse(b, "audio/mpeg"))
      )

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(a, b).good
    }

    "returns only MP3 urls if Opus transcoding is missing" in new Context {
      override lazy val transcodings = List(mp3Transcoding)

      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val c = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(a, b, None, Some(c)).good
    }
  }

}
