package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.jvmkit.{Urn, UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{NotFound, Result, ServerError, Success}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.publicApiStrangler.representation.{TrackRepresentation, TrackRepresentationLike}
import com.soundcloud.publicApiStrangler.TrackRepresentationsService
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.json
import com.soundcloud.service.response.representation.{Geoblockings, User}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.CollectorRegistry
import org.joda.time.LocalDateTime
import org.mockito.Mockito.when
import play.api.libs.json.Json

class SingleTrackControllerSpec extends InjectionBasedControllerSpecification {

  def trackmetadataTrack(
                          disabledAt: Option[LocalDateTime] = None,
                          isPublic: Boolean = true,
                          secretToken: String = "secr3t-Token",
                          isDownloadable: Option[Boolean] = Some(false),
                          user: Urn = new Urn("soundcloud:users:3000"),
                          label_id: Option[Int] = None,
                          reveal_stats: Boolean = false,
                          reveal_comments: Boolean = true) =
    Track(
      urn = new Urn("soundcloud:tracks:987"),
      user_urn = user,
      commentable = false,
      description = None,
      created_at = new LocalDateTime(2016, 5, 19, 18, 3, 4),
      disabled_at = disabledAt,
      downloadable = isDownloadable,
      duration = 0,
      genre = None,
      last_modified = new LocalDateTime(2016, 5, 19, 18, 3, 4),
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
      label_id = label_id
    )
  val user =
    User(
      urn = new Urn("soundcloud:users:3000"),
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

  val trackRepresentation = new TrackRepresentation(
    track = trackmetadataTrack(),
    user = user,
    isrc = None,
    counts = new StitchCounts(1, 2, 3, 4),
    label = None,
    geoblockings = None,
    domainlockings = Seq(),
    audioMetadata = new TrackAudioMetadata("lol", "donkey", 123))


  trait Context extends Scope {
    val fallback = mock[DispatchToMothershipHandler]
    val tracksService = mock[TrackRepresentationsService]

    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config, collectorRegistry)

    val session = new UserSessionBuilder().build()
    val trackUrn = new Urn("soundcloud:tracks:987")

    def controller(session: UserSession) = new SingleTrackController(
      fakeUserAuthentication(session),
      fallback,
      tracksService,
      telemetry)
  }

  val validPaths = List("/tracks/987", "/tracks/987/", "/tracks/987.json", "/tracks/987.json/")
  val nonNumericPaths = List("/tracks/__12", "/tracks/__12/", "/tracks/permalinktrack", "/tracks/permalinktrack/",
    "/tracks/permalinktrack.json", "/tracks/permalinktrack.json/")

  validPaths.foreach {
    path =>
      s"removes conditional request headers for path: $path" in new Context {
        when(fallback.dispatchToMothership(like[Request] {
          case r =>
            r.headerMap.get("If-None-Match") must beNone
        })).thenReturn(Future.value(Response()))
        when(tracksService.track(session, trackUrn, None, None)).thenReturn((Future.value(Success(trackRepresentation))))

        val response = get(controller(session), path, Map.empty, Map("If-None-Match" -> "a8d3ba6d09b68691b77dc75dfcd7a477"))
        response.status ==== Status.Ok
      }
  }

  nonNumericPaths.foreach {
    path =>
      s"returns 404 for non-numeric track identifier for path: $path" in new Context {
        val response = get(controller(session), path)
        response.status ==== Status.NotFound
        response.body ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
        response.getHeaders.get("Content-Length") must beSome("48")
      }
  }

  nonNumericPaths.foreach {
    path =>
      s"returns 404 wrapped in jsonp for non-numeric track identifier when callback param is provided for path: $path" in new Context {
        val response = get(controller(session), path, Map("callback" -> "js_callback_fn"))
        response.status ==== Status.NotFound
        response.body ==== """/**/js_callback_fn({"errors":[{"error_message":"404 - Not Found"}]});"""
        response.getHeaders.get("Content-Length") must beSome("69")
      }
  }

  validPaths.foreach {
    path =>
      s"Passes secret token to tracks service for path: $path" in new Context {
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(Response()))
        when(tracksService.track(session, trackUrn, Some("s3cret"), None)).thenReturn(Future.value(Success(trackRepresentation)))

        val response = get(controller(session), path, Map("secret_token" -> "s3cret"))
        response.status ==== Status.Ok
      }
  }

  validPaths.foreach {
    path =>
      s"Passes callback parameters to tracks service for path: $path" in new Context {
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(Response()))
        when(tracksService.track(session, trackUrn, None, Some("js_callback_dn"))).thenReturn(Future.value(Success(trackRepresentation)))

        val response = get(controller(session), path, Map("callback" -> "js_callback_dn"))
        response.status ==== Status.Ok
      }
  }

  validPaths.foreach {
    path =>
      s"When loading tracks from trackmetadata for: $path" >> {
        trait FromTrackMetadata extends Context {
          val defaultJsonResponse = """{"pass-through":"for sure"}"""

          def newResponse(code: Int) = {
            val response = Response()
            response.setContentString(defaultJsonResponse)
            response.setStatusCode(code)
            response
          }

          def trackRepresentationLike: Future[Result[TrackRepresentationLike]]

          when(tracksService.track(session, trackUrn, None, None)).thenReturn(trackRepresentationLike)
        }

        "it returns 200 for Some()" in new FromTrackMetadata {
          override def trackRepresentationLike = Future.value(Success(trackRepresentation))


          val response = get(controller(session), path)
          response.status.code ==== 200

          import TrackRepresentation.writes
          response.body ==== json.Json.stringify(trackRepresentation)
        }

        "it returns 404 None " in new FromTrackMetadata {
          override def trackRepresentationLike = Future.value(NotFound)

          val response = get(controller(session), path)
          response.status.code ==== 404
          response.body ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
        }

        "it returns 500 for failed futures" in new FromTrackMetadata {
          override def trackRepresentationLike = Future.exception(new RuntimeException("An unexpected error occured while fetching a track"))

          val response = get(controller(session), path)
          response.status.code ==== 500
          response.body ==== "An unexpected error occured while fetching a track"
        }
      }
  }
}
