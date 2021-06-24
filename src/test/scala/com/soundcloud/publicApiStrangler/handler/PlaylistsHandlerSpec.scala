package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.PlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistBuilder
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationSpecContext
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsDefined, JsString, Json}

class PlaylistsHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope with TrackRepresentationSpecContext with Scope {
    lazy val geo = new Geo("US")
    val playlist = new PlaylistBuilder().build
    lazy val session = new UserSessionBuilder()
      .setUser(Urn("soundcloud", "users", "2"))
      .setAgent(Urn("soundcloud", "applications", "v2"))
      .setGeo(geo)
      .build()

    val playlistDeletionClient = mock[PlaylistDeletionClient]
    val playlistsService = mock[PlaylistsService]
    val playlistUrn = Urn("soundcloud", "playlists", "1")

    lazy val handler = new PlaylistsHandler(
      new FakeUserAuthentication(session),
      playlistDeletionClient,
      playlistsService
    )

    override def routingDefinitions = Routing.forPlaylistHandler(handler)

    val requestedTrack1 = createTrackRepresentationFromVisibleTrack()
    val requestedTrack2 = createTrackRepresentationFromVisibleTrack()
    val access = AccessParams.defaultAccess
  }

  "DELETE /playlists/:id" >> {
    trait DeletePlaylistContext extends Context {
      val playlistId = 123
      lazy val deletePlaylistResponse: Outcome[Unit] = Good(())

      when(playlistDeletionClient.deletePlaylist(session, Urn("soundcloud", "playlists", playlistId.toString)))
        .thenReturn(Future.value(deletePlaylistResponse))

      val response = delete(s"/playlists/$playlistId")
    }

    "returns unauthorized for invalid session" in new DeletePlaylistContext {
      override lazy val session = anonymousSession

      response.status ==== Status.Unauthorized
    }

    "returns ok" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(())

      response.status ==== Status.Ok
      Json.parse(response.contentString) \ "status" ==== JsDefined(JsString("200 - OK"))
    }

    "returns not found" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = NotFound().bad

      response.status ==== Status.NotFound
      Json.parse(response.contentString) \ "status" ==== JsDefined(JsString("404 - Not Found"))
    }
  }

  "GET /playlists/:id" >> {
    "passes secret token to playlists service" in new Context {
      when(playlistsService.fetchPlaylist(session, playlistUrn, Some("s3cret"), access, None, None))
        .thenReturn(Future.value(Good(playlist)))
      get("/playlists/1", Map("secret_token" -> "s3cret"))
      verify(playlistsService).fetchPlaylist(session, playlistUrn, Some("s3cret"), access, None, None)
    }

    "it returns 200 when a playlist is found" in new Context {
      when(playlistsService.fetchPlaylist(session, playlistUrn, None, access, None, None))
        .thenReturn(Future.value(Good(playlist)))
      val response = get("/playlists/1")
      response.status.code ==== 200
    }

    "it returns 404 for None" in new Context {
      when(playlistsService.fetchPlaylist(session, playlistUrn, None, access, None, None))
        .thenReturn(Future.value(NotFound("playlist not found").bad))

      val response = get("/playlists/1")
      response.status.code ==== 404
    }
  }

  "GET /playlists/:id/tracks" >> {
    "returns track collection when playlist found" in new Context {
      val unpaginatedTracksCollection = Collection(List(requestedTrack1, requestedTrack2), None)

      when(playlistsService.fetchPlaylistTracks(session, playlistUrn, None, access, None))
        .thenReturn(Future.value(Good(unpaginatedTracksCollection)))

      val response = get("/playlists/1/tracks")
      response.status.code ==== 200
    }

    "returns paginated track collection" in new Context {
      val path = "/playlists/1/tracks?linked_partitioning=true&limit=1&secret_token=s-3creT"
      val mockRequest = Request(path)
      mockRequest.host = "localhost"
      val pagination = OffsetBasedPagination.build(mockRequest, Seq("linked_partitioning"))
      val paginatedTracksCollection =
        Collection(List(requestedTrack1, requestedTrack2), Some(pagination.normalizedHref))

      when(
        playlistsService
          .fetchPlaylistTracks(session, playlistUrn, Some("s-3creT"), access, Some(pagination))
      ).thenReturn(Future.value(Good(paginatedTracksCollection)))

      val response = get(path)
      response.status.code ==== 200
    }

    "returns 404 when no playlist found" in new Context {
      when(playlistsService.fetchPlaylistTracks(session, playlistUrn, None, access, None))
        .thenReturn(Future.value(Bad(NotFound("playlist not found"))))

      val response = get("/playlists/1/tracks")
      response.status.code ==== 404
    }
  }
}
