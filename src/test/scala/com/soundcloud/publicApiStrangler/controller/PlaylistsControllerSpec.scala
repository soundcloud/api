package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future

class PlaylistsControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val okidoki = mock[OkidokiClient]
    val session = new UserSessionBuilder().build()
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
    "delegates to mothership handler" in new Context {
      val response = delete(controller, "/playlists/1")

      response.status ==== Status.Ok
    }
  }
}
