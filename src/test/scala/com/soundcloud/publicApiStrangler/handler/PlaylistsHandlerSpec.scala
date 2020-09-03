package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.service.PlaylistsService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentationSpecContext, TracksCollection}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsDefined, JsString, Json}

class PlaylistsHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope with TrackRepresentationSpecContext {
    lazy val geo = new Geo("US")
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

    val requestedTrack1 = createTrackRepresentation()
    val requestedTrack2 = createTrackRepresentation()

    val user =
      User(
        urn = Urn("soundcloud", "users", "1"),
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg?123456789",
        permalink_url = "https://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"),
        updated_at = Some("2016/10/10 11:21:36 +0000")
      )

    val playlist =
      Playlist(
        title = "test",
        id = 1,
        duration = 120,
        userId = 1,
        kind = "playlist",
        releaseDay = None,
        permalinkUrl = "http://soundcloud.com/test",
        genre = "metal",
        permalink = "test",
        purchaseUrl = None,
        releaseMonth = None,
        description = None,
        uri = "http://soundcloud.com",
        labelName = None,
        label = None,
        tagList = "",
        releaseYear = None,
        trackCount = 1,
        lastModified = None,
        license = None,
        playlistType = "",
        downloadable = None,
        sharing = "",
        createdAt = None,
        release = None,
        purchaseTitle = None,
        artworkUrl = "",
        ean = None,
        streamable = Some(false),
        embeddableBy = "",
        labelId = None,
        user = user,
        tracks = List.empty,
        secretToken = None,
        secretUri = None
      )

  }

  "DELETE /playlists/:id" >> {
    trait DeletePlaylistContext extends Context {
      val playlistId = 123
      lazy val deletePlaylistResponse: Outcome[Status] = Good(Status.Ok)

      when(playlistDeletionClient.deletePlaylist(session, Urn("soundcloud", "playlists", playlistId.toString)))
        .thenReturn(Future.value(deletePlaylistResponse))

      val response = delete(s"/playlists/$playlistId")
    }

    "returns unauthorized for invalid session" in new DeletePlaylistContext {
      override lazy val session = anonymousSession

      response.status ==== Status.Unauthorized
    }

    "returns ok" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Ok)

      response.status ==== Status.Ok
      Json.parse(response.contentString) \ "status" ==== JsDefined(JsString("200 - OK"))
    }

    "returns accepted" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Accepted)

      response.status ==== Status.Accepted
      Json.parse(response.contentString) \ "status" ==== JsDefined(JsString("202 - Accepted"))
    }

    "returns unauthorized" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Unauthorized)

      response.status ==== Status.Unauthorized
      Json.parse(response.contentString) \ "status" ==== JsDefined(JsString("401 - Unauthorized"))
    }

    "returns forbidden" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.Forbidden)

      response.status ==== Status.Forbidden
      Json.parse(response.contentString) \ "status" ==== JsDefined(JsString("403 - Forbidden"))
    }

    "returns not found" in new DeletePlaylistContext {
      override lazy val deletePlaylistResponse = Good(Status.NotFound)

      response.status ==== Status.NotFound
      Json.parse(response.contentString) \ "status" ==== JsDefined(JsString("404 - Not Found"))
    }
  }

  "GET /playlists/:id" >> {
    "passes secret token to playlists service" in new Context {
      when(playlistsService.fetchPlaylist(session, playlistUrn, Some("s3cret"), None))
        .thenReturn(Future.value(Good(playlist)))
      get("/playlists/1", Map("secret_token" -> "s3cret"))
      verify(playlistsService).fetchPlaylist(session, playlistUrn, Some("s3cret"), None)
    }

    "it returns 200 when a playlist is found" in new Context {
      when(playlistsService.fetchPlaylist(session, playlistUrn, None, None))
        .thenReturn(Future.value(Good(playlist)))
      val response = get("/playlists/1")
      response.status.code ==== 200
    }

    "it returns 404 for None" in new Context {
      when(playlistsService.fetchPlaylist(session, playlistUrn, None, None))
        .thenReturn(Future.value(NotFound("playlist not found").bad))

      val response = get("/playlists/1")
      response.status.code ==== 404
    }
  }

  "GET /playlists/:id/tracks" >> {
    "returns track collection when playlist found" in new Context {
      val unpaginatedTracksCollection = TracksCollection(List(requestedTrack1, requestedTrack2), None)

      when(playlistsService.fetchPlaylistTracks(session, playlistUrn, None, None))
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
        TracksCollection(List(requestedTrack1, requestedTrack2), Some(pagination.normalizedHref))

      when(playlistsService.fetchPlaylistTracks(session, playlistUrn, Some("s-3creT"), Some(pagination)))
        .thenReturn(Future.value(Good(paginatedTracksCollection)))

      val response = get(path)
      response.status.code ==== 200
    }

    "returns 404 when no playlist found" in new Context {
      when(playlistsService.fetchPlaylistTracks(session, playlistUrn, None, None))
        .thenReturn(Future.value(Bad(NotFound("playlist not found"))))

      val response = get("/playlists/1/tracks")
      response.status.code ==== 404
    }
  }
}
