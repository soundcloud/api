package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.mothership.TrackAudioMetadata
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrackBuilder}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.Json

class SingleTrackHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {
  val visibleTrack = (new VisibleTrackBuilder)
    .setDisabledAt(None)
    .setPublic(true)
    .setSecretToken(Some("secr3t-Token"))
    .setDownloadable(true)
    .setUserUrn(Urn("soundcloud", "users", "3000"))
    .setLabelId(None)
    .setDownloadable(false)
    .setCommentable(false)
    .build

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

  val trackRepresentation = createTrackRepresentation(
    visibleTrack = visibleTrack,
    user = user,
    isrc = None,
    counts = new StitchCounts(1, 2, 3, 4, 5),
    label = None,
    geoblockings = List.empty,
    domainlockings = Seq(),
    audioMetadata = new TrackAudioMetadata("lol", Some("donkey"), Some(123))
  )

  trait Context extends HandlerSpecificationScope {
    val trackRepresentationsService = smartMock[TrackRepresentationsService]

    val telemetry = Telemetry.createIsolatedInstance

    val session = new UserSessionBuilder().build()
    val trackUrn = Urn("soundcloud", "tracks", "987")

    val handler = new SingleTrackHandler(
      new FakeUserAuthentication(session),
      trackRepresentationsService
    )

    override def routingDefinitions = Routing.forSingleTrackHandler(handler)
  }

  val path = "/tracks/987"

  val nonNumericPaths = List(
    "/tracks/__12",
    "/tracks/permalinktrack"
  )

  nonNumericPaths.foreach { path =>
    s"returns 400 for non-numeric track identifier for path: $path" in new Context {
      val response = get(path)
      response.status ==== Status.BadRequest
    }
  }

  "passes secret token to tracks service" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, Some("s3cret"))))
      .thenReturn(Future.value(Some(trackRepresentation)))

    get(path, Map("secret_token" -> "s3cret"))
    verify(trackRepresentationsService).track(session, TrackRequest(trackUrn, Some("s3cret")))
  }

  "it returns 200 when a track is found" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
      .thenReturn(Future.value(Some(trackRepresentation)))

    val response = get(path)
    response.status.code ==== 200

    response.contentString ==== Json.stringify(Json.toJson(trackRepresentation))
  }

  "it returns 404 for None" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
      .thenReturn(Future.None)

    val response = get(path)
    response.status.code ==== 404
    response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
  }
}
