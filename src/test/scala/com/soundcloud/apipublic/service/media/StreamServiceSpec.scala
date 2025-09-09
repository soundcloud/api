package com.soundcloud.apipublic.service.media

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.apipublic.authorization.policies.{ContentAuthorization, ContentPolicy, ContentRestriction, Reason}
import com.soundcloud.apipublic.client.tracks.{ContentAuthorizationBuilder, _}
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService.TrackWithTranscodingsFieldMask
import com.soundcloud.apipublic.service.{TrackVisibilityService, UnavailableByPolicy}
import com.soundcloud.apipublic.test.UnitSpecification
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

    lazy val transcodings = List(mp3ProgressiveAndHlsTranscoding, opusTranscoding)
    lazy val mp3ProgressiveAndHlsTranscoding =
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive", "hls"), None, "sq", 180000, None)
    lazy val mp3ProgressiveTranscoding =
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive"), None, "sq", 180000, None)
    lazy val mp3HlsTranscoding =
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("hls"), None, "sq", 180000, None)
    lazy val opusTranscoding =
      Transcoding("opus-uuid", "preset", """audio/ogg; codecs="opus"""", List("progressive"), None, "sq", 180000, None)
    lazy val aac160kTranscoding =
      Transcoding("aac-uuid", "aac_160k", """audio/mp4; codecs="mp4a.40.2"""", List("hls"), None, "sq", 180000, None)
    lazy val aac96kTranscoding =
      Transcoding("aac-uuid", "aac_96kk", """audio/mp4; codecs="mp4a.40.2"""", List("hls"), None, "sq", 180000, None)

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

    trackVisibilityService.tracks(
      session,
      List(trackRequest),
      TrackWithTranscodingsFieldMask,
      AccessParams.defaultAccess
    ) returns Future.value(tracks)
  }

  "error when no track is found" in new Context {
    override lazy val tracks = List[Outcome[VisibleTrack]]()
    Await.result(service.fetchUrls(session, trackUrn, secretToken)) ==== NotFound().bad
  }

  "error when track is not streamable" in new Context {
    lazy val unavailableError = CustomError(UnavailableByPolicy(trackUrn, Reason.NOT_SUPPORTED)).bad
    override lazy val tracks = List[Outcome[VisibleTrack]](unavailableError)
    Await.result(service.fetchUrls(session, trackUrn, secretToken)) ==== unavailableError
  }

  "error when no MP3 transodings are returned" in new Context {
    override lazy val transcodings = List(opusTranscoding)
    Await.result(service.fetchUrls(session, trackUrn, secretToken)) ==== NotFound().bad
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

    override lazy val transcodings = List(mp3ProgressiveAndHlsTranscoding)
    Await.result(service.fetchUrls(session, trackUrn, secretToken, singleStream = true)) ==== RedirectStreamResponse(
      "http://snippet"
    ).good
    Await.result(service.fetchUrls(session, trackUrn, secretToken)) ==== MediaStreamUrls(
      httpMp3 = Some("http://snippet"),
      hlsMp3 = Some("http://snippet")
    ).good
  }

  "#fetchUrls single" >> {
    "when track is streamable and policy is not BLOCK" >> {
      "Map NotFound response from Tracks to NotFound" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.NotFound, "stream not found")
        )

        tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.NotFound, "stream not found")
        )

        Await.result(service.fetchUrls(session, trackUrn, secretToken, singleStream = true)) ==== NotFound().bad
      }

      "Map Unauthenticated response from Tracks to NotAllowed" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
        )

        tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
        )

        Await.result(service.fetchUrls(session, trackUrn, secretToken, singleStream = true)) ==== NotAuthorized().bad
      }

      "returns an MP3 stream url" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.value(streamUrlTwirpResponse)

        val result = Await.result(service.fetchUrls(session, trackUrn, secretToken, singleStream = true))

        result ==== RedirectStreamResponse("http://stream").good
      }

      "returns an MP3 snippet url if policy is SNIP" in new Context {
        override lazy val policy = ContentPolicy.SNIP
        val result = Await.result(service.fetchUrls(session, trackUrn, secretToken, true))
        result ==== RedirectStreamResponse("http://snippet").good
      }
    }
  }

  "#fetchUrls multiple" >> {
    "Map NotFound response from Tracks to unavailableError outcome" in new Context {
      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "stream not found")
      )

      tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "stream not found")
      )

      Await.result(service.fetchUrls(session, trackUrn, secretToken)) ==== NotFound().bad
    }

    "Map Unauthenticated response from Tracks to not authorized" in new Context {
      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
      )

      tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
      )

      Await.result(service.fetchUrls(session, trackUrn, secretToken)) ==== NotAuthorized().bad
    }

    "existing track with new trancodings two bitrates: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac160kTranscoding, aac96kTranscoding, opusTranscoding)
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val aac160k = "http://stream/aac160k/hls"
      val aac96k = "http://stream/aac96k/hls"
      val e = "http://stream/opus/hls"
      val f = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
      Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2"""")),
      Future.value(GetMediaStreamResponse(e, """audio/ogg; codecs="opus"""")))

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), Some(aac96k), Some(aac160k), Some(e), Some(f)).good
    }

    "existing track with new trancodings with 160k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac160kTranscoding, opusTranscoding)
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val aac160k = "http://stream/aac160k/hls"
      val e = "http://stream/opus/hls"
      val f = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
      Future.value(GetMediaStreamResponse(e, """audio/ogg; codecs="opus"""")))

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), None, Some(aac160k), Some(e), Some(f)).good
    }

    "existing track with new trancodings wtih 96k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac96kTranscoding, opusTranscoding)
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val aac96k = "http://stream/aac96k/hls"
      val e = "http://stream/opus/hls"
      val f = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2"""")),
      Future.value(GetMediaStreamResponse(e, """audio/ogg; codecs="opus"""")))

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), Some(aac96k), None, Some(e), Some(f)).good
    }

    "existing track without new transcodings: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(mp3ProgressiveAndHlsTranscoding, opusTranscoding)
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val e = "http://stream/opus/hls"
      val f = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(e, """audio/ogg; codecs="opus"""")))

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), None, None, Some(e), Some(f)).good
    }

    "track with legacy hls transcodings and two bitrates: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(mp3HlsTranscoding, aac160kTranscoding, aac96kTranscoding)
      val b = "http://stream/mp3/hls"
      val aac160k = "http://stream/aac160k/hls"
      val aac96k = "http://stream/aac96k/hls"
      val f = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
        Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
        Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, Some(b), Some(aac96k), Some(aac160k), None, Some(f)).good
    }

    // Question: How to provide preview URLs when we have only aac
    "new track without legacy transcodings and two bitrates: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(aac160kTranscoding, aac96kTranscoding)
      val aac160k = "http://stream/aac160k/hls"
      val aac96k = "http://stream/aac96k/hls"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
        Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, None, Some(aac96k), Some(aac160k), None).good
    }

    "new track without legacy transcodings and 160k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(aac160kTranscoding)
      val aac160k = "http://stream/aac160k/hls"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, None, None, Some(aac160k), None).good
    }

    "new track without legacy transcodings and 96k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(aac96kTranscoding)
      val aac96k = "http://stream/aac96k/hls"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, None, Some(aac96k), None, None).good
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

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b)).good
    }

    "returns only MP3 urls if Opus transcoding is missing" in new Context {
      override lazy val transcodings = List(mp3ProgressiveAndHlsTranscoding)

      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val c = "http://snippet"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")))

      val result = Await.result(service.fetchUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), None, None, None, Some(c)).good
    }
  }

}
