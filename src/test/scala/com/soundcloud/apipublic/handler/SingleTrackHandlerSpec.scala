package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.tracks.{TrackRequest, VisibleTrackBuilder}
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
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
    .setDownloadable(false)
    .setCommentable(false)
    .build

  val user = new UserBuilder().build

  val trackRepresentation = createTrackRepresentationFromVisibleTrack(
    visibleTrack = visibleTrack,
    user = user,
    geoblockings = List.empty
  )
  val numericPath = "/tracks/987"
  val urnPath = "/tracks/soundcloud:tracks:987"

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

  "passes secret token to tracks service" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, Some("s3cret"))))
      .thenReturn(Future.value(Some(trackRepresentation)))

    get(urnPath, Map("secret_token" -> "s3cret"))
    verify(trackRepresentationsService).track(session, TrackRequest(trackUrn, Some("s3cret")))
  }

  "it returns 200 when a track urn is found" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
      .thenReturn(Future.value(Some(trackRepresentation)))

    val response = get(urnPath)
    response.status.code ==== 200

    response.contentString ==== Json.stringify(Json.toJson(trackRepresentation))
  }

  "it returns 200 when a track id is found" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
      .thenReturn(Future.value(Some(trackRepresentation)))

    val response = get(numericPath)
    response.status.code ==== 200

    response.contentString ==== Json.stringify(Json.toJson(trackRepresentation))
  }

  "it returns 404 for None" in new Context {
    when(trackRepresentationsService.track(session, TrackRequest(trackUrn, None)))
      .thenReturn(Future.None)

    val response = get(urnPath)
    response.status.code ==== 404
  }
}
