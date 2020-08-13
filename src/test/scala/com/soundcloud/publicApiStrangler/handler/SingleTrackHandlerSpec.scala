package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.mothership.TrackAudioMetadata
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.DateTime
import org.mockito.Mockito.when
import play.api.libs.json.Json

class SingleTrackHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {
  def trackmetadataTrack(
      disabledAt: Option[DateTime] = None,
      isPublic: Boolean = true,
      secretToken: String = "secr3t-Token",
      isDownloadable: Option[Boolean] = Some(false),
      user: Urn = Urn("soundcloud", "users", "3000"),
      label_id: Option[Long] = None,
      reveal_stats: Boolean = false,
      reveal_comments: Boolean = true
  ) =
    Track(
      urn = Urn("soundcloud", "tracks", "987"),
      user_urn = user,
      commentable = false,
      description = None,
      created_at = new DateTime(2016, 5, 19, 18, 3, 4),
      disabled_at = disabledAt,
      downloadable = isDownloadable,
      duration = 0,
      genre = None,
      last_modified = new DateTime(2016, 5, 19, 18, 3, 4),
      permalink = "",
      permalink_url = None,
      public = isPublic,
      secret_token = secretToken,
      user_tags = List.empty,
      machine_tags = List.empty,
      title = "",
      uid = Some("a1b2c3"),
      api_streamable = None,
      streamable = Some(false),
      reveal_comments = reveal_comments,
      reveal_stats = reveal_stats,
      label_name = None,
      license = "",
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
      label_id = label_id,
      supply_chain_status = None
    )

  val user =
    User(
      urn = Urn("soundcloud", "users", "3000"),
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
      updated_at = Some("2016/10/10 11:21:36 +0000")
    )

  val mockTrackRepresentation = createTrackRepresentation(
    track = trackmetadataTrack(),
    user = user,
    isrc = None,
    counts = new StitchCounts(1, 2, 3, 4, 5),
    label = None,
    geoblockings = List.empty,
    domainlockings = Seq(),
    audioMetadata = new TrackAudioMetadata("lol", Some("donkey"), Some(123))
  )

  trait Context extends HandlerSpecificationScope {
    val trackRepresentationsService = mock[TrackRepresentationsService]

    val telemetry = Telemetry.createIsolatedInstance

    val session = new UserSessionBuilder().build()
    val trackUrn = Urn("soundcloud", "tracks", "987")

    val handler = new SingleTrackHandler(
      new FakeUserAuthentication(session),
      trackRepresentationsService,
      telemetry
    )

    override def routingDefinitions = Routing.forSingleTrackHandler(handler)
  }

  val path = "/tracks/987"

  val nonNumericPaths = List(
    "/tracks/__12",
    "/tracks/permalinktrack"
  )

  val expectedJson = Json.parse("""
      {
    |    "artwork_url": null,
    |    "available_country_codes": null,
    |    "bpm": 120.7,
    |    "comment_count": null,
    |    "commentable": false,
    |    "created_at": "2016/05/19 18:03:04 +0000",
    |    "description": null,
    |    "domain_lockings": null,
    |    "download_count": null,
    |    "download_url": "https://api.soundcloud.com/tracks/987/download",
    |    "downloadable": false,
    |    "downloads_remaining": null,
    |    "duration": 0,
    |    "embeddable_by": "none",
    |    "favoritings_count": null,
    |    "genre": null,
    |    "id": 987,
    |    "isrc": null,
    |    "key_signature": "Emaj",
    |    "kind": "track",
    |    "label": null,
    |    "label_id": null,
    |    "label_name": null,
    |    "last_modified": "2016/05/19 18:03:04 +0000",
    |    "license": "",
    |    "original_content_size": 123,
    |    "original_format": "donkey",
    |    "permalink": "",
    |    "permalink_url": null,
    |    "playback_count": null,
    |    "purchase_title": "buy me pls",
    |    "purchase_url": "http://example.com/buy/7890",
    |    "release": "DR012",
    |    "release_day": null,
    |    "release_month": null,
    |    "release_year": null,
    |    "reposts_count": null,
    |    "secret_token": null,
    |    "secret_uri": null,
    |    "sharing": "public",
    |    "state": "lol",
    |    "stream_url": "https://api.soundcloud.com/tracks/987/stream",
    |    "streamable": null,
    |    "tag_list": "",
    |    "title": "",
    |    "track_type": "original",
    |    "uri": "https://api.soundcloud.com/tracks/987",
    |    "user": {
    |        "avatar_url": "https://example.com/giraffe.jpg",
    |        "id": 3000,
    |        "kind": "user",
    |        "last_modified": "2016/10/10 11:21:36 +0000",
    |        "permalink": "giraffe",
    |        "permalink_url": "http://soundcloud.com/denis",
    |        "uri": "https://api.soundcloud.com/users/3000",
    |        "username": "Dr. G. Raffe"
    |    },
    |    "user_favorite": false,
    |    "user_id": 3000,
    |    "user_playback_count": 1,
    |    "user_uri": "https://api.soundcloud.com/users/3000",
    |    "video_url": "http://example.com/video.mp4",
    |    "waveform_url": "https://bar.sndcdn.com/stream/a1b2c3.png"
    |} 
    """.stripMargin)

  s"removes conditional request headers for path: $path" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
      .thenReturn(Future.value(Some(mockTrackRepresentation)))

    val response =
      get(path, Map.empty, Map("If-None-Match" -> "a8d3ba6d09b68691b77dc75dfcd7a477"))

    response.status ==== Status.Ok
    Json.parse(response.contentString) ==== expectedJson
  }

  nonNumericPaths.foreach { path =>
    s"returns 404 for non-numeric track identifier for path: $path" in new Context {
      val response = get(path)
      response.status ==== Status.NotFound
      response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
    }
  }

  s"Passes secret token to tracks service for path: $path" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, Some("s3cret"))))
      .thenReturn(Future.value(Some(mockTrackRepresentation)))

    val response = get(path, Map("secret_token" -> "s3cret"))
    response.status ==== Status.Ok
    Json.parse(response.contentString) ==== expectedJson
  }

  s"When loading tracks from trackmetadata for: $path" >> {
    trait FromTrackMetadata extends Context {
      def trackRepresentation: Future[Option[TrackRepresentation]]

      when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
        .thenReturn(trackRepresentation)

    }

    "it returns 200 for Some()" in new FromTrackMetadata {
      override def trackRepresentation: Future[Option[TrackRepresentation]] =
        Future.value(Some(mockTrackRepresentation))

      val response = get(path)
      response.status.code ==== 200

      response.contentString ==== Json.stringify(Json.toJson(mockTrackRepresentation))
    }

    "it returns 404 for None" in new FromTrackMetadata {
      override def trackRepresentation = Future.value(None)

      val response = get(path)
      response.status.code ==== 404
      response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
    }
  }
}
