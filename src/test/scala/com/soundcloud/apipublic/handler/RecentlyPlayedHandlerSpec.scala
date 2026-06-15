package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.RecentlyPlayedService
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentationSpecContext
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.twitter.finagle.http.Status
import com.twitter.util.Future

class RecentlyPlayedHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {

  trait Context extends HandlerSpecificationScope {
    val userUrn = Urn("soundcloud", "users", "10419549")
    lazy val session = new UserSessionBuilder().setUser(userUrn).build()
    val recentlyPlayedService = mock[RecentlyPlayedService]

    lazy val handler = new RecentlyPlayedHandler(
      new FakeUserAuthentication(session),
      recentlyPlayedService
    )

    override def routingDefinitions = Routing.forRecentlyPlayedHandler(handler)
  }

  "GET /me/recently-played/tracks" >> {
    trait GetRecentlyPlayedTracksContext extends Context {
      val tracks = List(createTrackRepresentationFromVisibleTrack())

      recentlyPlayedService.recentlyPlayedTracks(session, userUrn, AccessParams.explicitAccess) returns Future.value(
        tracks
      )

      val response = get("/me/recently-played/tracks")
    }

    "returns 200" in new GetRecentlyPlayedTracksContext {
      response.status ==== Status.Ok
    }

    "returns a list of tracks" in new GetRecentlyPlayedTracksContext {
      val expectedResponse = Collection.getNonNullRepresentation(Collection(tracks, None), hasLinkedPartitioning = true)
      response.contentString ==== expectedResponse
    }
  }
}
