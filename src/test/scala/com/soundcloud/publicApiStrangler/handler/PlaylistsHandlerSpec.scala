package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.ModuleConversions._
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.experimental.result.{Good, Result}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.scalakit.Geo
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.mockito.Mockito.when
import play.api.libs.json.{JsString, Json}

class PlaylistsHandlerSpec extends UnitSpecification with Fixtures {

  trait Context extends HandlerSpecificationScope {
    lazy val geo = Geo("US")
    lazy val session = new UserSessionBuilder()
      .setUser(Urn("soundcloud:users:2"))
      .setAgent(Urn("soundcloud:applications:v2"))
      .setGeo(geo)
      .build()

    val playlistDeletionClient = mock[PlaylistDeletionClient]

    lazy val handler = new PlaylistsHandler(new FakeUserAuthentication(session), playlistDeletionClient)

    override def routingDefinitions = Routing.forPlaylistHandler(handler)
  }

  "DELETE /playlists/:id" >> {
    trait DeletePlaylistContext extends Context {
      val playlistId = 123
      lazy val deletePlaylistResponse: Result[Status] = Good(Status.Ok)

      when(playlistDeletionClient.deletePlaylist(session, Urn(s"soundcloud:playlists:$playlistId")))
        .thenReturn(Future.value(deletePlaylistResponse))

      val response = delete(handler.handleDelete _, s"/playlists/$playlistId")
    }

    "returns unauthorized for invalid session" in new DeletePlaylistContext {
      override lazy val session = anonymousSession

      response.status ==== Status.Unauthorized
    }

    "returns ok" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Ok)

      response.status ==== Status.Ok
      Json.parse(response.contentString) \ "status" ==== JsString("200 - OK")
    }

    "returns accepted" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Accepted)

      response.status ==== Status.Accepted
      Json.parse(response.contentString) \ "status" ==== JsString("202 - Accepted")
    }

    "returns unauthorized" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Unauthorized)

      response.status ==== Status.Unauthorized
      Json.parse(response.contentString) \ "status" ==== JsString("401 - Unauthorized")
    }

    "returns forbidden" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Forbidden)

      response.status ==== Status.Forbidden
      Json.parse(response.contentString) \ "status" ==== JsString("403 - Forbidden")
    }

    "returns not found" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.NotFound)

      response.status ==== Status.NotFound
      Json.parse(response.contentString) \ "status" ==== JsString("404 - Not Found")
    }
  }
}
