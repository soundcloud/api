package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Geo, Urn}
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.{DeletePlaylistResponse, ForbiddenDeletePlaylistResponse, InvalidUrnDeletePlaylistResponse, NotAuthorizedDeletePlaylistResponse, OkDeletePlaylistResponse}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future

class PlaylistsControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    lazy val geo = Geo("US")
    lazy val session = new UserSessionBuilder()
      .setUser(Urn("soundcloud:users:2"))
      .setAgent(Urn("soundcloud:applications:v2"))
      .setGeo(geo)
      .build()

    val okidoki = mock[OkidokiClient]
    val mothershipHandler = mock[DispatchToMothershipHandler]

    lazy val controller = new PlaylistsController(fakeUserAuthentication(session), okidoki, mothershipHandler)

    when(mothershipHandler.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }

  "POST /playlists" >> {
    "delegates to mothership handler" in new Context {
      val response = post(controller, "/playlists")

      response.status ==== Status.Ok
    }
  }

  "PUT /playlists/:id" >> {
    "delegates to mothership handler" in new Context {
      val response = put(controller, "/playlists/1")

      response.status ==== Status.Ok
    }
  }

  "DELETE /playlists/:id" >> {
    trait DeletePlaylistContext extends Context {
      val playlistId = 123
      lazy val deletePlaylistResponse: DeletePlaylistResponse = OkDeletePlaylistResponse

      when(okidoki.deletePlaylist(session, Urn(s"soundcloud:playlists:$playlistId")))
        .thenReturn(Future.value(deletePlaylistResponse))

      val response = delete(controller, s"/playlists/$playlistId")
    }

    "returns unauthorized for invalid session" in new DeletePlaylistContext {
      override lazy val session = anonymousSession

      response.status ==== Status.Unauthorized
    }

    "returns ok" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = OkDeletePlaylistResponse

      response.status ==== Status.Ok
    }

    "returns unauthorized" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = NotAuthorizedDeletePlaylistResponse

      response.status ==== Status.Unauthorized
    }

    "returns forbidden" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = ForbiddenDeletePlaylistResponse

      response.status ==== Status.Forbidden
    }

    "returns not found" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = InvalidUrnDeletePlaylistResponse

      response.status ==== Status.NotFound
    }
  }
}
