package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{
  ContentAuthorization,
  ContentPolicy,
  ContentRestriction
}
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import com.soundcloud.publicApiStrangler.client.tracks.{ContentAuthorizationBuilder, _}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.joda.time.{DateTime, LocalDateTime}

class StreamServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val tracksClient = mock[TracksClient]
    val service = new StreamService(tracksClient)
    val session = mock[UserSession]

    val track = mock[Track]
    val trackUid = "some-uid"
    val trackUrn = Urn("soundcloud", "tracks", "2")
    val userUrn = Urn("soundcloud", "users", "42")

    val secretToken = Some("secret")
    lazy val maybeStreamable = Some(true)

    lazy val uid: Option[String] = Some(trackUid)
    lazy val disabledAt: Option[DateTime] = None

    track.uid returns uid
    track.api_streamable returns maybeStreamable
    track.user_urn returns userUrn
    track.disabled_at returns disabledAt

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
    lazy val tracks = List(visibleTrack)

    lazy val streamUrlResponse = StreamUrlResponse("http://stream", "audio/mpeg")
    lazy val streamPreviewUrlResponse = StreamUrlResponse("http://snippet", "audio/mpeg")

    tracksClient.streamUrl(any[UserSession], any[StreamRequest]) returns Future.value(streamUrlResponse)
    tracksClient.previewUrl(any[UserSession], any[StreamRequest]) returns Future.value(streamPreviewUrlResponse)
    tracksClient.visibleTracks(session, List(trackRequest)) returns Future.value(tracks)
  }

  "error when no track is found" in new Context {
    override lazy val tracks = List[VisibleTrack]()
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
  }

  "error when track is found but disabled (taken down or over quota)" in new Context {
    override lazy val tracks = List(visibleTrack.copy(disabledAt = Some(LocalDateTime.now())))
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
  }

  "error when no transcodings are returned" in new Context {
    override lazy val transcodings = List.empty[Transcoding]
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
  }

  "error when no MP3 transodings are returned" in new Context {
    override lazy val transcodings = List(opusTranscoding)
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== MediaStreamNotFoundError
  }

  "downgrades to snippet when MP3 transodings are returned but progressive streaming restricted" in new Context {
    override lazy val tracks = List(
      visibleTrack.copy(authorization = new ContentAuthorization(
        visibleTrack.urn,
        visibleTrack.authorization.getPolicy,
        visibleTrack.authorization.getReason,
        Set[ContentRestriction](ContentRestriction.NO_PROGRESSIVE_DOWNLOAD),
        visibleTrack.authorization.getMonetizationModel
      )
      )
    )

    override lazy val transcodings = List(mp3Transcoding)
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== MediaStreamUrl("http://snippet")
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== PreviewUrls(
      "http://snippet",
      "http://snippet"
    )
  }

  "#fetchSingle" >> {
    "when track is streamable and policy is not BLOCK" >> {
      "returns an MP3 stream url" in new Context {
        tracksClient.streamUrl(session, streamRequest) returns Future.value(streamUrlResponse)
        val result = Await.result(service.fetchSingle(session, trackUrn, secretToken))

        result ==== MediaStreamUrl("http://stream")
      }

      "returns an MP3 snippet url if policy is SNIP" in new Context {
        override lazy val policy = ContentPolicy.SNIP
        val result = Await.result(service.fetchSingle(session, trackUrn, secretToken))
        result ==== MediaStreamUrl("http://snippet")
      }
    }
  }

  "#fetchMultiple" >> {
    "returns multiple stream urls and one snippet url" in new Context {
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val c = "http://stream/opus/hls"
      val d = "http://snippet"

      tracksClient.streamUrl(any[UserSession], any[StreamRequest]) returns (Future.value(
        StreamUrlResponse(a, "audio/mpeg")
      ),
      Future.value(StreamUrlResponse(b, "audio/mpeg")),
      Future.value(StreamUrlResponse(c, """audio/ogg; codecs="opus"""")))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(a, b, Some(c), d)
    }

    "returns multiple snippet urls if policy is SNIP" in new Context {
      override lazy val policy = ContentPolicy.SNIP
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"

      tracksClient.previewUrl(any[UserSession], any[StreamRequest]) returns (Future.value(
        StreamUrlResponse(a, "audio/mpeg")
      ),
      Future.value(StreamUrlResponse(b, "audio/mpeg")))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== PreviewUrls(a, b)
    }

    "returns only MP3 urls if Opus transcoding is missing" in new Context {
      override lazy val transcodings = List(mp3Transcoding)

      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val c = "http://snippet"

      tracksClient.streamUrl(any[UserSession], any[StreamRequest]) returns (Future.value(
        StreamUrlResponse(a, "audio/mpeg")
      ),
      Future.value(StreamUrlResponse(b, "audio/mpeg")))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== MediaStreamUrls(a, b, None, c)
    }
  }

}
