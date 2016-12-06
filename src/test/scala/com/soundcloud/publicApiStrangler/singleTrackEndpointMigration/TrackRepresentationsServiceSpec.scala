package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.{Urn, UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.mediaservice.{MediaServiceUrlGenClient, WaveformUrl}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission, Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.client.LieblingClient
import com.soundcloud.service.response.representation._
import com.soundcloud.service.response.representation.liebling.UserLikesCount
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
import org.mockito.Mockito._
import play.api.libs.json._

class TrackRepresentationsServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val trackRepresentationWrites = TrackRepresentation.writes

    val trackmetadataClient = mock[TrackmetadataClient]
    val okidokiClient = mock[RichOkidokiClient]
    val pubmeseClient = mock[PubmeseClient]
    val stitchClient = mock[StitchClient]
    val lieblingClient = mock[LieblingClient]
    val mediaUrlGenClient = mock[MediaServiceUrlGenClient]
    val userQuotaClient = mock[UserQuotaClient]

    val tracksService = new TrackRepresentationsService(
      trackmetadataClient,
      okidokiClient,
      pubmeseClient,
      stitchClient,
      lieblingClient,
      mediaUrlGenClient,
      userQuotaClient
    )

    val requestingUserUrn = new Urn("soundcloud:users:112")
    val labelUrn = new Urn("soundcloud:users:678")
    val trackOwnerUrn = new Urn("soundcloud:users:3000")

    def trackOwner =
      User(
        urn = trackOwnerUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "http://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"),
        updated_at = Some("2016/10/10 11:21:36 +0000"))

    def requestingUser =
      User(
        urn = requestingUserUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "http://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"),
        updated_at = Some("2016/10/10 11:21:36 +0000"))

    def label =
      User(
        urn = labelUrn,
        permalink = "raz",
        username = "Raz Putin",
        avatar_url = "http://example.com/raz.jpg",
        permalink_url = "https://soundcloud.com/raz",
        city = None,
        country = None,
        tracks_count = 4,
        followers_count = Some(10000),
        followings_count = Some(10),
        verified = true,
        description = Some("Psychonaut Music Inc."),
        updated_at = Some("2016/10/10 11:21:36 +0000"))

    val trackUrn = Urn("soundcloud:tracks:987")
    val createdAt = new LocalDateTime(2016, 5, 19, 18, 3, 4)
    val lastModified = new LocalDateTime(2016, 5, 20, 18, 3, 4)

    def geoblockings: Option[Geoblockings] = Some(List("DE", "FR"))

    def domainLockings: Seq[DomainLocking] = Seq(
      DomainLocking(
        domain = "example.com",
        urn = Urn("soundcloud:domain-lockings:1"),
        trackUrn = Urn("soundcloud:tracks:123")
      )
    )

    def trackAudioMetadata =
      TrackAudioMetadata(
        state = "failed",
        original_content_size = 9001,
        original_format = "vqf"
      )

    def trackmetadataTrack(
      disabledAt: Option[LocalDateTime] = None,
      isPublic: Boolean = true,
      secretToken: String = "secr3t-Token",
      isDownloadable: Boolean = false,
      user: Urn = trackOwnerUrn,
      label_id: Option[Int] = Some(labelUrn.getIdentifier.toInt),
      reveal_stats: Boolean = false,
      reveal_comments: Boolean = true) =
      Track(
        urn = trackUrn,
        user_urn = user,
        commentable = false,
        description = None,
        created_at = createdAt,
        disabled_at = disabledAt,
        downloadable = isDownloadable,
        duration = 0,
        genre = None,
        last_modified = lastModified,
        permalink = null,
        permalink_url = None,
        public = isPublic,
        secret_token = secretToken,
        user_tags = List.empty,
        machine_tags = List.empty,
        title = null,
        uid = Some("a1b2c3"),
        api_streamable = None,
        streamable = false,
        reveal_comments = reveal_comments,
        reveal_stats = reveal_stats,
        label_name = None,
        license = null,
        embeddable = None,
        release_year = None,
        release_month = None,
        release_day = None,
        embeddableBy = EmbeddingPermission.None,
        releaseDate = None,
        artwork = Artwork(None),
        published_at = None,
        purchase_url = Some("http://example.com/buy/7890"),
        purchase_title = Some("buy me pls"),
        bpm = Some(120.7),
        track_type = Some("original"),
        release = Some("DR012"),
        key_signature = Some("Emaj"),
        video_url = Some("http://example.com/video.mp4"),
        label_id = label_id
      )

    def isrc(wrapped: String = "US-S1Z-99-00001"): Option[Isrc] =
      Some(Isrc(wrapped))

    def stitchCounts: StitchCounts =
      StitchCounts(111, 222, 333, 444)

    def userLikesCount: UserLikesCount =
      UserLikesCount(Set.empty, List.empty)

    def waveformUrls: Seq[WaveformUrl] =
      Seq(
        WaveformUrl("stream",
          "https://foo.sndcdn.com/stream/a1b2c3.json",
          "https://bar.sndcdn.com/stream/a1b2c3.png"),
        WaveformUrl("preview",
          "https://foo.sndcdn.com/preview/a1b2c3.json",
          "https://bar.sndcdn.com/preview/a1b2c3.png"))

    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()

    def setUpMocksForExistingTrack(track: Track, session: UserSession) = {
      when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
      when(okidokiClient.fetchUserObjects(session, Set(requestingUserUrn))).thenReturn(Future.value(List(requestingUser)))
      when(okidokiClient.fetchUserObjects(session, Set(labelUrn))).thenReturn(Future.value(List(label)))
      when(okidokiClient.fetchUserObjects(session, Set(trackOwnerUrn))).thenReturn(Future.value(List(trackOwner)))
      when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))
      when(stitchClient.countsForTrack(session, trackUrn, trackOwnerUrn)).thenReturn(Future.value(stitchCounts))
      when(okidokiClient.fetchTrackGeoblockings(session, trackUrn)).thenReturn(Future.value(geoblockings))
      when(okidokiClient.fetchTrackDomainLockings(session, trackUrn)).thenReturn(Future.value(domainLockings))
      when(okidokiClient.fetchTrackAudioMetadata(session, trackUrn)).thenReturn(Future.value(Some(trackAudioMetadata)))
      when(lieblingClient.userLikeCounts(session, List(trackUrn), session.getUser)).thenReturn(Future.value(userLikesCount))
      when(mediaUrlGenClient.waveformUrls(session, track.uid)).thenReturn(Future.value(waveformUrls))
      when(userQuotaClient.downloadsPerTrack(session, Set(track.user_urn))).thenReturn(Future.value(Map.empty[Urn, Option[Int]]))
    }

    def setUpMocksForNonExistingTrack = {
      when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.None)
      when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))
      when(okidokiClient.fetchTrackGeoblockings(session, trackUrn)).thenReturn(Future.value(geoblockings))
      when(okidokiClient.fetchTrackDomainLockings(session, trackUrn)).thenReturn(Future.value(domainLockings))
      when(okidokiClient.fetchTrackAudioMetadata(session, trackUrn)).thenReturn(Future.value(Some(trackAudioMetadata)))
    }
  }

  "Returns 200 for public tracks" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.Ok
    Json.fromResponse(response) \ "id" ==== JsNumber(track.urn.getIdentifier.toInt)
  }

  "Wraps track data in jsonp if `callback` param is defined" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)

    val response = Await.result(tracksService.track(session, trackUrn, None, Some("js_callback_fn")))

    response.status ==== Status.Ok
    response.contentString must startWith("/**/js_callback_fn(")
    response.contentString must endWith(");")
  }

  "Returns a response with the right headers" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.headerMap.get("Content-Length") must beSome(response.contentString.getBytes("UTF-8").length.toString)
    response.headerMap.get("Content-Type") must beSome("application/json; charset=utf-8")
  }

  "Returns 404 for non existing tracks" in new Context {
    setUpMocksForNonExistingTrack

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Wraps error message in jsonp if `callback` param is defined" in new Context {
    setUpMocksForNonExistingTrack

    val response = Await.result(tracksService.track(session, trackUrn, None, Some("js_callback_fn")))
    response.status ==== Status.NotFound
    response.contentString ==== """/**/js_callback_fn({"errors":[{"error_message":"404 - Not Found"}]});"""
  }

  "Returns 404 response with the right headers" in new Context {
    setUpMocksForNonExistingTrack

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.headerMap.get("Content-Length") must beSome("48")
    response.headerMap.get("Content-Type") must beSome("application/json; charset=utf-8")
  }

  "Returns 404 when track is disabled" in new Context {
    val disabledAt = Some(LocalDateTime.now())
    val track = trackmetadataTrack(disabledAt)
    setUpMocksForNonExistingTrack

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 404 when track is not public" in new Context {
    val track = trackmetadataTrack(isPublic = false)
    setUpMocksForNonExistingTrack

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if the owner is requesting" in new Context {
    val track = trackmetadataTrack(isPublic = false)
    val ownerSession = new UserSessionBuilder().setUser(trackOwnerUrn).build
    setUpMocksForExistingTrack(track, ownerSession)

    val response = Await.result(tracksService.track(ownerSession, trackUrn, None, None))
    response.status ==== Status.Ok
  }

  "Returns 404 for private tracks if there is an incorrect secret token" in new Context {
    val wrongSecretToken = "secr3tTokenWRONG"
    val track = trackmetadataTrack(isPublic = false)
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(Future.value(Some(track)))
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.value(isrc()))
    when(okidokiClient.fetchTrackGeoblockings(session, trackUrn)).thenReturn(Future.value(geoblockings))
    when(okidokiClient.fetchTrackDomainLockings(session, trackUrn)).thenReturn(Future.value(domainLockings))
    when(okidokiClient.fetchTrackAudioMetadata(session, trackUrn)).thenReturn(Future.value(Some(trackAudioMetadata)))

    val response = Await.result(tracksService.track(session, trackUrn, Some(wrongSecretToken), None))
    response.status ==== Status.NotFound
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }

  "Returns 200 for private tracks if there is a correct secret token" in new Context {
    val correctSecretToken = "aSecre_t"
    val track = trackmetadataTrack(isPublic = false, secretToken = correctSecretToken)
    setUpMocksForExistingTrack(track, session)

    val response = Await.result(tracksService.track(session, trackUrn, Some(correctSecretToken), None))
    response.status ==== Status.Ok
  }

  "Returns 404 for public tracks if user does not exist" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(okidokiClient.fetchUserObjects(session, Set(trackOwnerUrn))).thenReturn(Future.value(List.empty))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.NotFound
  }

  "Returns 503 for public tracks if user can not be fetched" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(okidokiClient.fetchUserObjects(session, Set(trackOwnerUrn))).thenReturn(Future.exception(new Exception("asd")))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.ServiceUnavailable
    response.contentString ==== """{"errors":[{"error_message":"503 - Service Unavailable"}]}"""
    response.headerMap.get("Content-Length") must beSome("58")
  }

  "Returns 503 for public tracks if label can not be fetched" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(okidokiClient.fetchUserObjects(session, Set(labelUrn))).thenReturn(Future.exception(new Exception("asd")))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))
    response.status ==== Status.ServiceUnavailable
    response.contentString ==== """{"errors":[{"error_message":"503 - Service Unavailable"}]}"""
    response.headerMap.get("Content-Length") must beSome("58")
  }

  "Returns empty ISRC when Pubmese is failing" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(pubmeseClient.isrcForTrack(session, trackUrn)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.Ok
    Json.fromResponse(response) \ "isrc" ==== JsNull
  }

  "Returns no geoblockings if Moshimoshi is failing" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(okidokiClient.fetchTrackGeoblockings(session, trackUrn)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.Ok
    Json.fromResponse(response).as[JsObject].keys.contains("available_country_codes") ==== false
  }

  "Returns no geoblockings if Moshimoshi returns an empty list" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(okidokiClient.fetchTrackGeoblockings(session, trackUrn)).thenReturn(Future.value(Some(List())))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.Ok
    Json.fromResponse(response).as[JsObject].keys.contains("available_country_codes") ==== false
  }

  "Returns empty domainlockings if Moshimoshi is failing" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(okidokiClient.fetchTrackDomainLockings(session, trackUrn)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.Ok
    Json.fromResponse(response).as[JsObject].keys.contains("domain_lockings") ==== false
  }

  "Returns 503 if Moshimoshi is failing for the audio endpoint" in new Context {
    val track = trackmetadataTrack()
    setUpMocksForExistingTrack(track, session)
    when(okidokiClient.fetchTrackAudioMetadata(session, trackUrn)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

    val response = Await.result(tracksService.track(session, trackUrn, None, None))

    response.status ==== Status.ServiceUnavailable
    response.contentString ==== """{"errors":[{"error_message":"503 - Service Unavailable"}]}"""
    response.headerMap.get("Content-Length") must beSome("58")
  }

  "user_favorite" >> {
    "is true when the user has favourited the track, and is logged in" in new Context {
      val track = trackmetadataTrack()
      override val userLikesCount = UserLikesCount(Set(track.urn), List.empty)
      override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
      setUpMocksForExistingTrack(track, session)
      when(lieblingClient.userLikeCounts(session, List(trackUrn), session.getUser)).thenReturn(Future.value(userLikesCount))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))

      response.status ==== Status.Ok
      val json = Json.fromResponse(response)
      json \ "user_favorite" ==== JsBoolean(true)
    }

    "is false when the user has not favourited the track, and is logged in" in new Context {
      val track = trackmetadataTrack()
      override val userLikesCount = UserLikesCount(Set.empty, List.empty)
      override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
      setUpMocksForExistingTrack(track, session)
      when(lieblingClient.userLikeCounts(session, List(trackUrn), session.getUser)).thenReturn(Future.value(userLikesCount))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))

      response.status ==== Status.Ok
      val json = Json.fromResponse(response)
      json \ "user_favorite" ==== JsBoolean(false)
    }

    "is not present when the user is not logged in" in new Context {
      val track = trackmetadataTrack()
      override val session = anonymousSession
      setUpMocksForExistingTrack(track, session)

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      response.status ==== Status.Ok
      Json.fromResponse(response).as[JsObject].keys.contains("user_favorite") ==== false
    }
  }

  "user_playback_count" >> {
    "is always 1 when the user is logged in" in new Context {
      val track = trackmetadataTrack()
      override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
      setUpMocksForExistingTrack(track, session)
      when(lieblingClient.userLikeCounts(session, List(trackUrn), session.getUser)).thenReturn(Future.value(userLikesCount))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      val json = Json.fromResponse(response)
      json \ "user_playback_count" ==== JsNumber(1)
    }

    "is not present when the user is not logged in" in new Context {
      val track = trackmetadataTrack()
      override val session = anonymousSession
      session.isAnonymous ==== true
      setUpMocksForExistingTrack(track, session)

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      Json.fromResponse(response).as[JsObject].keys.contains("user_playback_count") ==== false
    }
  }

  "waveform_url" >> {
    "is present when urlgen returns a stream URL" in new Context {
      val track = trackmetadataTrack()
      setUpMocksForExistingTrack(track, session)

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      Json.fromResponse(response).as[JsObject].keys.contains("waveform_url") ==== true
    }

    "it not present when urlgen returns no stream URLs" in new Context {
      val track = trackmetadataTrack()
      setUpMocksForExistingTrack(track, session)
      when(mediaUrlGenClient.waveformUrls(session, track.uid)).thenReturn(Future.value(Seq.empty))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      Json.fromResponse(response).as[JsObject].keys.contains("waveform_url") ==== false
    }

    "it is not present when urlgen fails" in new Context {
      val track = trackmetadataTrack()
      setUpMocksForExistingTrack(track, session)
      when(mediaUrlGenClient.waveformUrls(session, track.uid)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      Json.fromResponse(response).as[JsObject].keys.contains("waveform_url") ==== false
    }
  }

  "label" >> {
    "is present when track has a a label" in new Context {
      val track = trackmetadataTrack()
      setUpMocksForExistingTrack(track, session)

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      Json.fromResponse(response).as[JsObject].keys.contains("label") ==== true
    }

    "it not present when urlgen returns no stream URLs" in new Context {
      val track = trackmetadataTrack(label_id = None)
      setUpMocksForExistingTrack(track, session)

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      Json.fromResponse(response).as[JsObject].keys.contains("label") ==== false
    }
  }

  "secret_token parameter in URIs" >> {
    "appends the secret token when one is given" in new Context {
      val track = trackmetadataTrack().copy(permalink_url = Some("http://soundcloud.com/foo/bar"))
      setUpMocksForExistingTrack(track, session)

      val response = Await.result(tracksService.track(session, trackUrn, Some("s-4kT0a"), None))
      val json = Json.fromResponse(response)
      json \ "uri" ==== JsString("https://api.soundcloud.com/tracks/987?secret_token=s-4kT0a")
      json \ "stream_url" ==== JsString("https://api.soundcloud.com/tracks/987/stream?secret_token=s-4kT0a")
      json \ "download_url" ==== JsString("https://api.soundcloud.com/tracks/987/download?secret_token=s-4kT0a")
      json \ "permalink_url" ==== JsString("http://soundcloud.com/foo/bar/s-4kT0a")
    }

    "does not add a secret token to null values" in new Context {
      val track = trackmetadataTrack().copy(permalink_url = None)
      setUpMocksForExistingTrack(track, session)

      val response = Await.result(tracksService.track(session, trackUrn, Some("s-4kT0a"), None))
      val json = Json.fromResponse(response)
      json \ "permalink_url" ==== JsNull
    }
  }

  "downloadable" >> {
    "is true when track is downloadable, and below user's quota" in new Context {
      val track = trackmetadataTrack(isDownloadable = true)
      setUpMocksForExistingTrack(track, session)
      userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> Some(100)))
      stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 90, 0, 0))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      val json = Json.fromResponse(response)
      json \ "downloadable" ==== JsBoolean(true)
    }

    "it true when track is downloadable, and use has no quota" in new Context {
      val track = trackmetadataTrack(isDownloadable = true)
      setUpMocksForExistingTrack(track, session)
      userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> None))
      stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 90, 0, 0))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      val json = Json.fromResponse(response)
      json \ "downloadable" ==== JsBoolean(true)
    }

    "is false when track is downloadable, and above user's quota" in new Context {
      val track = trackmetadataTrack(isDownloadable = true)
      setUpMocksForExistingTrack(track, session)
      userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> Some(100)))
      stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 100, 0, 0))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      val json = Json.fromResponse(response)
      json \ "downloadable" ==== JsBoolean(false)
    }

    "is false when track is not downloadable, and below user's quota" in new Context {
      val track = trackmetadataTrack(isDownloadable = false)
      setUpMocksForExistingTrack(track, session)
      userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> Some(100)))
      stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 90, 0, 0))

      val response = Await.result(tracksService.track(session, trackUrn, None, None))
      val json = Json.fromResponse(response)
      json \ "downloadable" ==== JsBoolean(false)
    }
  }

  "downloads_remaining" >> {

    "when the requesting user is not the owner of the track" >> {
      "it is not shown" in new Context {
        val track = trackmetadataTrack(isDownloadable = true)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> Some(100)))
        stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 90, 0, 0))

        val response = Await.result(tracksService.track(session, trackUrn, None, None))
        Json.fromResponse(response).as[JsObject].keys.contains("downloads_remaining") ==== false
      }
    }

    "when the requesting user is the owner of the track" >> {
      "is shown when track is below quota" in new Context {
        val track = trackmetadataTrack(isDownloadable = true, user = session.getUser)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> Some(100)))
        stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 90, 0, 0))

        val response = Await.result(tracksService.track(session, trackUrn, None, None))
        val json = Json.fromResponse(response)
        json \ "downloads_remaining" ==== JsNumber(10)
      }

      "it not shown when the track's user has no quota (eg. is unlimited)" in new Context {
        val track = trackmetadataTrack(isDownloadable = true, user = session.getUser)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> None))
        stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 90, 0, 0))

        val response = Await.result(tracksService.track(session, trackUrn, None, None))
        Json.fromResponse(response).as[JsObject].keys.contains("downloads_remaining") ==== false
      }

      "is shown when no downloads remain" in new Context {
        val track = trackmetadataTrack(isDownloadable = true, user = session.getUser)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> Some(100)))
        stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 100, 0, 0))

        val response = Await.result(tracksService.track(session, trackUrn, None, None))
        val json = Json.fromResponse(response)
        json \ "downloads_remaining" ==== JsNumber(0)
      }

      "is shown even if track is not downloadable" in new Context {
        val track = trackmetadataTrack(isDownloadable = false, user = session.getUser)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.user_urn)) returns Future.value(Map(track.user_urn -> Some(100)))
        stitchClient.countsForTrack(session, trackUrn, track.user_urn) returns Future.value(StitchCounts(0, 90, 0, 0))

        val response = Await.result(tracksService.track(session, trackUrn, None, None))
        val json = Json.fromResponse(response)
        json \ "downloads_remaining" ==== JsNumber(10)
      }
    }
  }

  "counts" >> {
    "requesting as uploader" >> {
      "returns proper counts" in new Context {
        override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

        val track = trackmetadataTrack()
        setUpMocksForExistingTrack(track, session)

        val response = Await.result(tracksService.track(session, trackUrn, None, None))

        response.status ==== Status.Ok
        val json = Json.fromResponse(response)
        json \ "playback_count" ==== JsNumber(111)
        json \ "download_count" ==== JsNumber(222)
        json \ "favoritings_count" ==== JsNumber(333)
      }

      "returns empty counts if Stitch is failing" in new Context {
        override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

        val track = trackmetadataTrack()
        setUpMocksForExistingTrack(track, session)
        when(stitchClient.countsForTrack(session, trackUrn, trackOwnerUrn)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

        val response = Await.result(tracksService.track(session, trackUrn, None, None))

        response.status ==== Status.Ok
        val json = Json.fromResponse(response)
        json \ "playback_count" ==== JsNumber(0)
        json \ "download_count" ==== JsNumber(0)
        json \ "favoritings_count" ==== JsNumber(0)
        json \ "comment_count" ==== JsNumber(0)
      }

      "includes comment_count if reveal_comments = true" in new Context {
        override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

        val track = trackmetadataTrack()
        setUpMocksForExistingTrack(track, session)

        val response = Await.result(tracksService.track(session, trackUrn, None, None))

        response.status ==== Status.Ok
        val json = Json.fromResponse(response)
        json \ "comment_count" ==== JsNumber(444)
      }

      "does not include comment_count if reveal_comments = false" in new Context {
        override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

        val track = trackmetadataTrack(reveal_comments = false)
        setUpMocksForExistingTrack(track, session)

        val response = Await.result(tracksService.track(session, trackUrn, None, None))

        response.status ==== Status.Ok
        val json = Json.fromResponse(response)
        json.as[JsObject].keys.contains("comment_count") ==== false
      }
    }

    "not requesting as uploader" >> {
      "track stats are not public" >> {
        "returns no counts" in new Context {
          val track = trackmetadataTrack()
          setUpMocksForExistingTrack(track, session)

          val response = Await.result(tracksService.track(session, trackUrn, None, None))

          response.status ==== Status.Ok
          val json = Json.fromResponse(response)
          json.as[JsObject].keys.contains("playback_count") ==== false
          json.as[JsObject].keys.contains("download_count") ==== false
          json.as[JsObject].keys.contains("favoritings_count") ==== false
          json.as[JsObject].keys.contains("comment_count") ==== false
        }
      }

      "track has public stats" >> {
        "returns proper counts" in new Context {
          val track = trackmetadataTrack(reveal_stats = true)
          setUpMocksForExistingTrack(track, session)

          val response = Await.result(tracksService.track(session, trackUrn, None, None))

          response.status ==== Status.Ok
          val json = Json.fromResponse(response)
          json \ "playback_count" ==== JsNumber(111)
          json \ "download_count" ==== JsNumber(222)
          json \ "favoritings_count" ==== JsNumber(333)
          json \ "comment_count" ==== JsNumber(444)
        }

        "includes comment_count if reveal_comments = true" in new Context {
          val track = trackmetadataTrack(reveal_stats = true)
          setUpMocksForExistingTrack(track, session)

          val response = Await.result(tracksService.track(session, trackUrn, None, None))

          response.status ==== Status.Ok
          val json = Json.fromResponse(response)
          json \ "comment_count" ==== JsNumber(444)
        }

        "does not include comment_count if reveal_comments = false" in new Context {
          val track = trackmetadataTrack(reveal_stats = true, reveal_comments = false)
          setUpMocksForExistingTrack(track, session)

          val response = Await.result(tracksService.track(session, trackUrn, None, None))

          response.status ==== Status.Ok
          val json = Json.fromResponse(response)
          json.as[JsObject].keys.contains("comment_count") ==== false
        }

        "returns empty counts if Stitch is failing" in new Context {
          val track = trackmetadataTrack(reveal_stats = true)
          setUpMocksForExistingTrack(track, session)
          when(stitchClient.countsForTrack(session, trackUrn, trackOwnerUrn)).thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

          val response = Await.result(tracksService.track(session, trackUrn, None, None))

          response.status ==== Status.Ok
          val json = Json.fromResponse(response)
          json \ "playback_count" ==== JsNumber(0)
          json \ "download_count" ==== JsNumber(0)
          json \ "favoritings_count" ==== JsNumber(0)
          json \ "comment_count" ==== JsNumber(0)
        }
      }
    }
  }
}
