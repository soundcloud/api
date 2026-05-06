package com.soundcloud.apipublic.service.media

import com.soundcloud.apipublic.authorization.policies.{
  Access,
  ContentAuthorization,
  ContentPolicy,
  ContentRestriction,
  Reason
}
import com.soundcloud.apipublic.client.tracks._
import com.soundcloud.apipublic.handler.PlayParams
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService.TrackWithTranscodingsFieldMask
import com.soundcloud.apipublic.service.{TrackVisibilityService, UnavailableByPolicy}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
import proto.soundcloud.tracks.api.{GetMediaStreamRequest, GetMediaStreamResponse, MediaService}

class StreamServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackVisibilityService = mock[TrackVisibilityService]
    val tracksMediaTwirpClient = mock[MediaService]

    val baseUrl = "https://api-test.soundcloud.com"
    val service = new StreamService(trackVisibilityService, tracksMediaTwirpClient, baseUrl)
    val session = new UserSessionBuilder().build()

    val track = mock[VisibleTrack]
    val trackUid = "some-uid"
    val trackUrn = Urn("soundcloud", "tracks", "2")
    val userUrn = Urn("soundcloud", "users", "42")

    lazy val secretToken: Option[String] = None
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

    lazy val transcodings = List(mp3ProgressiveAndHlsTranscoding, aac160kTranscoding)
    lazy val mp3ProgressiveAndHlsTranscoding =
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive", "hls"), None, "sq", 180000, None)
    lazy val mp3ProgressiveTranscoding =
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive"), None, "sq", 180000, None)
    lazy val mp3HlsTranscoding =
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("hls"), None, "sq", 180000, None)
    lazy val aac160kTranscoding =
      Transcoding(
        "aac-160-uuid",
        "aac_160k",
        """audio/mp4; codecs="mp4a.40.2"""",
        List("hls"),
        None,
        "sq",
        180000,
        None
      )
    lazy val aac96kTranscoding =
      Transcoding("aac-96-uuid", "aac_96kk", """audio/mp4; codecs="mp4a.40.2"""", List("hls"), None, "sq", 180000, None)

    lazy val visibleTrack = new VisibleTrackBuilder()
      .setUrn(trackUrn)
      .setUid(uid)
      .setPublic(true)
      .setSecretToken(secretToken)
      .setAuthorization(contentAuth)
      .setTranscodings(transcodings)
      .build

    val trackRequest = TrackRequest(trackUrn, secretToken)
    val trackClientResponse = mock[TrackRequest]
    lazy val tracks = List(visibleTrack.good)

    lazy val streamUrlTwirpResponse = GetMediaStreamResponse(
      "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http",
      "audio/mpeg"
    )
    lazy val streamPreviewUrlTwirpResponse = GetMediaStreamResponse(
      "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview",
      "audio/mpeg"
    )

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

    Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken)) ==== NotFound().bad
    Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== NotFound().bad
  }

  "error when track is not streamable" in new Context {
    lazy val unavailableError = CustomError(UnavailableByPolicy(trackUrn, Reason.NOT_SUPPORTED)).bad
    override lazy val tracks = List[Outcome[VisibleTrack]](unavailableError)

    Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken)) ==== unavailableError
    Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== unavailableError
  }

  "error when no MP3 transodings are returned for legacy stream" in new Context {
    override lazy val transcodings = List()
    Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== NotFound().bad
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

    Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== RedirectStreamResponse(
      "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"
    ).good

    Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uui", "http"))) ====
      RedirectStreamResponse("https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview").good
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

        Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== NotFound().bad
        Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uuid", "http"))) ==== NotFound().bad
        Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uuid", "hls"))) ==== NotFound().bad
      }

      "Map Unauthenticated response from Tracks to NotAllowed" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
        )

        tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
          TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
        )

        Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== NotAuthorized().bad
        Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uuid", "http"))) ==== NotAuthorized().bad
      }

      "returns an MP3 stream url" in new Context {
        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.value(streamUrlTwirpResponse)

        Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== RedirectStreamResponse(
          "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"
        ).good

        Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uuid", "http-preview"))) ==== RedirectStreamResponse(
          "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"
        ).good
      }

      "returns an AAC stream url" in new Context {

        tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
          Future.value(
            GetMediaStreamResponse(
              "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls",
              """audio/mp4; codecs="mp4a.40.2""""
            )
          )
        )

        Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "aac-160-uuid", "hls"))) ==== RedirectStreamResponse(
          "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls"
        ).good
      }

      "returns an MP3 snippet url if policy is SNIP" in new Context {
        override lazy val policy = ContentPolicy.SNIP

        val result = Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken))
        result ==== RedirectStreamResponse(
          "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"
        ).good
      }

      "downgrades progressive and hls play URLs to preview when track access is Preview" in new Context {
        override lazy val visibleTrack =
          new VisibleTrackBuilder()
            .setUrn(trackUrn)
            .setUid(uid)
            .setPublic(true)
            .setSecretToken(secretToken)
            .setAuthorization(contentAuth)
            .setTranscodings(transcodings)
            .setAccess(Some(Access.Preview))
            .build

        tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.value(
          streamPreviewUrlTwirpResponse
        )

        Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uuid", "http"))) ====
          RedirectStreamResponse(
            "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"
          ).good

        tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.value(
          GetMediaStreamResponse(
            "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls-preview",
            """audio/mp4; codecs="mp4a.40.2""""
          )
        )

        Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "aac-160-uuid", "hls"))) ====
          RedirectStreamResponse(
            "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls-preview"
          ).good
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

      Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== NotFound().bad
      Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uuid", "http"))) ==== NotFound().bad
    }

    "Map Unauthenticated response from Tracks to not authorized" in new Context {
      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
      )

      tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns Future.exception(
        TwinagleException(ErrorCode.Unauthenticated, "stream not authorised")
      )

      Await.result(service.fetchLegacyProgressivePreviewTranscodingUrl(session, trackUrn, secretToken)) ==== NotAuthorized().bad
      Await.result(service.fetchStreamUrl(session, PlayParams(trackUrn, secretToken, "mp3-uuid", "hls"))) ==== NotAuthorized().bad
    }

    "existing track with new trancodings two bitrates: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac160kTranscoding, aac96kTranscoding)
      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http"
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls"
      val aac160k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls"
      val aac96k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-96-uuid/hls"
      val f = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
      Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2"""")))

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), Some(aac96k), Some(aac160k), Some(f)).good
    }

    "existing track with new trancodings two bitrates: returns multiple stream urls and one snippet url and keeps secret" in new Context {
      override lazy val secretToken: Option[String] = Some("secret")
      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac160kTranscoding, aac96kTranscoding)
      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http?secret_token=secret"
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls?secret_token=secret"
      val aac160k =
        "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls?secret_token=secret"
      val aac96k =
        "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-96-uuid/hls?secret_token=secret"
      val f =
        "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview?secret_token=secret"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
      Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2"""")))

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), Some(aac96k), Some(aac160k), Some(f)).good
    }

    "existing track with new trancodings with 160k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac160kTranscoding)
      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http"
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls"
      val aac160k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls"
      val f = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")))

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), None, Some(aac160k), Some(f)).good
    }

    "existing track with new trancodings wtih 96k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac96kTranscoding)
      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http"
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls"
      val aac96k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-96-uuid/hls"
      val f = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
      Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2"""")))

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), Some(aac96k), None, Some(f)).good
    }

    "existing track without new transcodings: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(mp3ProgressiveAndHlsTranscoding)
      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http"
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls"
      val f = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")))

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), None, None, Some(f)).good
    }

    "track with legacy hls transcodings and two bitrates: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(mp3HlsTranscoding, aac160kTranscoding, aac96kTranscoding)
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls"
      val aac160k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls"
      val aac96k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-96-uuid/hls"
      val f = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(b, "audio/mpeg")),
        Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
        Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, Some(b), Some(aac96k), Some(aac160k), Some(f)).good
    }

    // Question: How to provide preview URLs when we have only aac
    "new track without legacy transcodings and two bitrates: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(aac160kTranscoding, aac96kTranscoding)
      val aac160k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls"
      val aac96k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-96-uuid/hls"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2"""")),
        Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, None, Some(aac96k), Some(aac160k), None).good
    }

    "new track without legacy transcodings and 160k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(aac160kTranscoding)
      val aac160k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-160-uuid/hls"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(aac160k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, None, None, Some(aac160k), None).good
    }

    "new track without legacy transcodings and 96k bitrate only: returns multiple stream urls and one snippet url" in new Context {
      override lazy val transcodings: List[Transcoding] = List(aac96kTranscoding)
      val aac96k = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/aac-96-uuid/hls"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(aac96k, """audio/mp4; codecs="mp4a.40.2""""))
      )

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(None, None, Some(aac96k), None, None).good
    }

    "returns multiple snippet urls if policy is SNIP" in new Context {
      override lazy val policy = ContentPolicy.SNIP
      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls-preview"

      tracksMediaTwirpClient.getMediaPreview(any[GetMediaStreamRequest]) returns (
        Future.value(GetMediaStreamResponse(a, "audio/mpeg")),
        Future.value(GetMediaStreamResponse(b, "audio/mpeg"))
      )

      val result: Outcome[MediaStreamUrls] = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result match {
        case Good(urls) =>
          urls.httpMp3 ==== Some(a)
          urls.hlsMp3 ==== Some(b)
        case Bad(_) =>
          failure("Expected Good result")
      }
    }

    "returns only preview_mp3_128_url for supply-chain tracks" in new Context {
      override lazy val visibleTrack =
        new VisibleTrackBuilder()
          .setUrn(trackUrn)
          .setUid(uid)
          .setPublic(true)
          .setSecretToken(secretToken)
          .setAuthorization(contentAuth)
          .setTranscodings(transcodings)
          .setSupplyChainStatus(Some(TrackVisibilityService.SUPPLY_CHAIN_STATUS_SUPPLY_CHAIN))
          .build

      override lazy val transcodings: List[Transcoding] =
        List(mp3ProgressiveAndHlsTranscoding, aac160kTranscoding, aac96kTranscoding)

      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"

      val result: Outcome[MediaStreamUrls] = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ====
        MediaStreamUrls(None, None, None, None, Some(a)).good
    }

    "returns only MP3 urls if Opus transcoding is missing" in new Context {
      override lazy val transcodings = List(mp3ProgressiveAndHlsTranscoding)

      val a = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http"
      val b = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/hls"
      val c = "https://api-test.soundcloud.com/tracks/soundcloud:tracks:2/streams/mp3-uuid/http-preview"

      tracksMediaTwirpClient.getMediaStream(any[GetMediaStreamRequest]) returns (Future.value(
        GetMediaStreamResponse(a, "audio/mpeg")
      ),
      Future.value(GetMediaStreamResponse(b, "audio/mpeg")))

      val result = Await.result(service.fetchTranscodingUrls(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(Some(a), Some(b), None, None, Some(c)).good
    }
  }

}
