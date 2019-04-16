package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.client.media.{MediaServiceClient, Transcoding}
import com.soundcloud.publicApiStrangler.client.tracks.{TracksClient, VisibleTrack}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}

class StreamServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val tracksClient = mock[TracksClient]
    val mediaServiceClient = mock[MediaServiceClient]
    val session = mock[UserSession]
    val service = new StreamService(tracksClient, mediaServiceClient)

    val trackUrn = Urn("soundcloud", "tracks", "2")
    val userUrn = Urn("soundcloud", "users", "42")
    val trackUid = "some-uid"
    val secretToken = Some("secret")

    lazy val maybeStreamable = Some(true)
    lazy val policy: ContentPolicy = ContentPolicy.ALLOW
    val auth = new ContentAuthorization(trackUrn, policy, Reason.DEFAULT, MonetizationModel.NOT_APPLICABLE)
    val maybeTrack: Option[VisibleTrack] = Some(VisibleTrack(trackUrn, userUrn, Some(trackUid), maybeStreamable, false, auth))
    lazy val transcodings = List(
      Transcoding("mp3-uuid", "audio/mpeg"),
      Transcoding("opus-uuid", """audio/ogg; codecs="opus"""")
    )

    tracksClient.visibleTrack(session, trackUrn, secretToken) returns Future.value(maybeTrack)
    mediaServiceClient.fetchTranscodings(session, trackUid) returns Future.value(transcodings)
  }

  "error when no track is found" in new Context {
    override val maybeTrack = None
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== StreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== StreamNotFoundError
  }

  "error when no track has no uid" in new Context {
    override val maybeTrack = Some(VisibleTrack(trackUrn, userUrn, None, maybeStreamable, false, auth))
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== StreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== StreamNotFoundError
  }

  "error when track is not streamable" in new Context {
    override lazy val maybeStreamable = Some(false)
    session.getUser returns Urn("soundcloud", "users", "1000")
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== StreamNotAllowed
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== StreamNotAllowed
  }

  "error when content policy is BLOCK" in new Context {
    override lazy val policy = ContentPolicy.BLOCK
    session.getUser returns Urn("soundcloud", "users", "1000")
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== StreamNotAllowed
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== StreamNotAllowed
  }

  "error when no transcodings are returned" in new Context {
    override lazy val transcodings = List.empty[Transcoding]
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== StreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== StreamNotFoundError
  }

  "error when no MP3 transodings are returned" in new Context {
    override lazy val transcodings = List(Transcoding("aac-uuid", "audio/aac"))
    Await.result(service.fetchSingle(session, trackUrn, secretToken)) ==== StreamNotFoundError
    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== StreamNotFoundError
  }

  "error when a stream url request returns a 404" in new Context {
    mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "progressive") returns Future.value(Some("url"))
    mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "hls") returns Future.None
    mediaServiceClient.fetchStreamUrl(session, "opus-uuid", "hls") returns Future.value(Some("url"))
    mediaServiceClient.fetchPreviewUrl(session, "mp3-uuid", "progressive") returns Future.value(Some("url"))

    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) ==== StreamNotFoundError
  }

  "exception when a stream url request returns a 500" in new Context {
    mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "progressive") returns Future.value(Some("url"))
    mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "hls") returns Future.value(Some("url"))
    mediaServiceClient.fetchStreamUrl(session, "opus-uuid", "hls") returns Future.exception(new Exception)
    mediaServiceClient.fetchPreviewUrl(session, "mp3-uuid", "progressive") returns Future.value(Some("url"))

    Await.result(service.fetchMultiple(session, trackUrn, secretToken)) should throwAn[Exception]
  }

  "#fetchSingle" >> {
    "when track is streamable and policy is not BLOCK" >> {
      "returns an MP3 stream url" in new Context {
        mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "progressive") returns Future.value(Some("http://stream"))
        val result = Await.result(service.fetchSingle(session, trackUrn, secretToken))
        result ==== StreamUrl("http://stream")
      }

      "returns an MP3 snippet url if policy is SNIP" in new Context {
        override lazy val policy = ContentPolicy.SNIP
        mediaServiceClient.fetchPreviewUrl(session, "mp3-uuid", "progressive") returns Future.value(Some("http://snippet"))
        val result = Await.result(service.fetchSingle(session, trackUrn, secretToken))
        result ==== StreamUrl("http://snippet")
      }
    }

    "when track is not streamable and policy is BLOCK, but the streamer is the track's owner" >> {
      "returns an MP3 stream url" in new Context {
        override lazy val maybeStreamable = Some(false)
        override lazy val policy = ContentPolicy.BLOCK
        session.getUser returns userUrn
        mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "progressive") returns Future.value(Some("http://stream"))
        val result = Await.result(service.fetchSingle(session, trackUrn, secretToken))
        result ==== StreamUrl("http://stream")
      }
    }
  }

  "#fetchMultiple" >> {
    "returns multiple stream urls and one snippet url" in new Context {
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val c = "http://stream/opus/hls"
      val d = "http://snippet"
      mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "progressive") returns Future.value(Some(a))
      mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "hls") returns Future.value(Some(b))
      mediaServiceClient.fetchStreamUrl(session, "opus-uuid", "hls") returns Future.value(Some(c))
      mediaServiceClient.fetchPreviewUrl(session, "mp3-uuid", "progressive") returns Future.value(Some(d))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== StreamUrls(a, b, Some(c), d)
    }

    "returns multiple snippet urls if policy is SNIP" in new Context {
      override lazy val policy = ContentPolicy.SNIP
      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      mediaServiceClient.fetchPreviewUrl(session, "mp3-uuid", "progressive") returns Future.value(Some(a))
      mediaServiceClient.fetchPreviewUrl(session, "mp3-uuid", "hls") returns Future.value(Some(b))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== PreviewUrls(a, b)
    }

    "returns only MP3 urls if Opus transcoding is missing" in new Context {
      override lazy val transcodings = List(
        Transcoding("mp3-uuid", "audio/mpeg"),
      )

      val a = "http://stream/mp3/progressive"
      val b = "http://stream/mp3/hls"
      val c = "http://snippet"
      mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "progressive") returns Future.value(Some(a))
      mediaServiceClient.fetchStreamUrl(session, "mp3-uuid", "hls") returns Future.value(Some(b))
      mediaServiceClient.fetchPreviewUrl(session, "mp3-uuid", "progressive") returns Future.value(Some(c))

      val result = Await.result(service.fetchMultiple(session, trackUrn, secretToken))
      result ==== StreamUrls(a, b, None, c)
    }
  }
}
