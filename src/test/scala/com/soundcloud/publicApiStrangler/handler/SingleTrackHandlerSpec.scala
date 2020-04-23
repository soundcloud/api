package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
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
  TrackRepresentationLike,
  TrackRepresentationsService
}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.DateTime
import org.mockito.Mockito.when
import play.api.libs.json.Json

class SingleTrackHandlerSpec extends UnitSpecification {
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
      permalink = null,
      permalink_url = None,
      public = isPublic,
      secret_token = secretToken,
      user_tags = List.empty,
      machine_tags = List.empty,
      title = null,
      uid = Some("a1b2c3"),
      api_streamable = None,
      streamable = Some(false),
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

  val trackRepresentation = new TrackRepresentation(
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
    val exceptionCollector = new ExceptionCollector(telemetry)

    val session = new UserSessionBuilder().build()
    val trackUrn = Urn("soundcloud", "tracks", "987")

    val handler = new SingleTrackHandler(
      new FakeUserAuthentication(session),
      trackRepresentationsService,
      telemetry,
      exceptionCollector
    )

    override def routingDefinitions = Routing.forSingleTrackHandler(handler)
  }

  val validPaths = List("/tracks/987", "/tracks/987/", "/tracks/987.json", "/tracks/987.json/")
  val nonNumericPaths = List(
    "/tracks/__12",
    "/tracks/__12/",
    "/tracks/permalinktrack",
    "/tracks/permalinktrack/",
    "/tracks/permalinktrack.json",
    "/tracks/permalinktrack.json/"
  )

  val expectedJson = Json.parse("""
      |{
      |"kind": "track",
      |"id": 987,
      |"created_at": "2016/05/19 18:03:04 +0000",
      |"user_id": 3000,
      |"duration": 0,
      |"commentable": false,
      |"state": "lol",
      |"original_content_size": 123,
      |"last_modified": "2016/05/19 18:03:04 +0000",
      |"sharing": "public",
      |"tag_list": "",
      |"permalink": null,
      |"streamable": null,
      |"embeddable_by": "none",
      |"purchase_url": "http://example.com/buy/7890",
      |"purchase_title": "buy me pls",
      |"label_id": null,
      |"genre": null,
      |"title": null,
      |"description": null,
      |"label_name": null,
      |"release": "DR012",
      |"track_type": "original",
      |"key_signature": "Emaj",
      |"isrc": null,
      |"video_url": "http://example.com/video.mp4",
      |"bpm": 120.7,
      |"release_year": null,
      |"release_month": null,
      |"release_day": null,
      |"original_format": "donkey",
      |"license": null,
      |"uri": "https://api.soundcloud.com/tracks/987",
      |"user": {
      |  "id": 3000,
      |  "kind": "user",
      |  "permalink": "giraffe",
      |  "username": "Dr. G. Raffe",
      |  "last_modified": "2016/10/10 11:21:36 +0000",
      |  "uri": "https://api.soundcloud.com/users/3000",
      |  "permalink_url": "http://soundcloud.com/denis",
      |  "avatar_url": "https://example.com/giraffe.jpg"
      |},
      |"permalink_url": null,
      |"artwork_url": null,
      |"stream_url": "https://api.soundcloud.com/tracks/987/stream",
      |"download_url": "https://api.soundcloud.com/tracks/987/download"
      |}
    """.stripMargin)

  validPaths.foreach { path =>
    s"removes conditional request headers for path: $path" in new Context {
      when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
        .thenReturn(Future.value(Some(trackRepresentation)))

      val response =
        get(path, Map.empty, Map("If-None-Match" -> "a8d3ba6d09b68691b77dc75dfcd7a477"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== expectedJson
    }
  }

  nonNumericPaths.foreach { path =>
    s"returns 404 for non-numeric track identifier for path: $path" in new Context {
      val response = get(path)
      response.status ==== Status.NotFound
      response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
      response.headerMap.get("Content-Length") must beSome("48")
    }
  }

  validPaths.foreach { path =>
    s"Passes secret token to tracks service for path: $path" in new Context {

      when(trackRepresentationsService.track(session, TrackRequest(trackUrn, Some("s3cret"))))
        .thenReturn(Future.value(Some(trackRepresentation)))

      val response = get(path, Map("secret_token" -> "s3cret"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== expectedJson
    }
  }

  validPaths.foreach { path =>
    s"When loading tracks from trackmetadata for: $path" >> {
      trait FromTrackMetadata extends Context {
        def trackRepresentationLike: Future[Option[TrackRepresentationLike]]

        when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
          .thenReturn(trackRepresentationLike)

      }

      "it returns 200 for Some()" in new FromTrackMetadata {
        override def trackRepresentationLike = Future.value(Some(trackRepresentation))

        val response = get(path)
        response.status.code ==== 200

        import TrackRepresentation.writes

        response.contentString ==== Json.stringify(Json.toJson(trackRepresentation))
      }

      "it returns 404 for None" in new FromTrackMetadata {
        override def trackRepresentationLike = Future.value(None)

        val response = get(path)
        response.status.code ==== 404
        response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
      }

      "it returns 500 for failed futures" in new FromTrackMetadata {
        override def trackRepresentationLike =
          Future.exception(new RuntimeException("An unexpected error occurred while fetching a track"))

        val response = get(path)
        response.status.code ==== 500
        response.contentString ==== "An unexpected error occured while fetching a track"
      }
    }
  }
}
