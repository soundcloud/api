package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.mothership.request.representation.Value
import com.soundcloud.apipublic.client.playlists.PlaylistDeletionClient
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.PlaylistsService
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.representation.{Playlist, PlaylistCreateOrUpdate}
import com.soundcloud.apipublic.service.playlists.{PlaylistBuilder, UpdatePlaylistArtworkRequest}
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentationSpecContext
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.twitter.finagle.http.{FileElement, Request, Status}
import com.twitter.io.{BufReader, Reader}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsDefined, JsString, Json}

class PlaylistsHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope with TrackRepresentationSpecContext with Scope {

    val nullPlaylist = PlaylistCreateOrUpdate()

    lazy val geo = new Geo("US")
    val playlist = new PlaylistBuilder().build

    lazy val session = new UserSessionBuilder()
      .setUser(Urn("soundcloud", "users", "2"))
      .setAgent(Urn("soundcloud", "applications", "v2"))
      .setGeo(geo)
      .build()
    val rollout = mock[Rollout]
    val playlistDeletionClient = mock[PlaylistDeletionClient]
    val playlistsService = mock[PlaylistsService]
    val playlistUrn = Urn("soundcloud", "playlists", "1")

    val baseUrl = "http://localhost:5000"

    lazy val handler = new PlaylistsHandler(
      new FakeUserAuthentication(session),
      playlistDeletionClient,
      playlistsService,
      baseUrl,
      new ExceptionCollector(Telemetry.createIsolatedInstance)
    )

    override def routingDefinitions = Routing.forPlaylistHandler(handler)

    val requestedTrack1 = createTrackRepresentationFromVisibleTrack()
    val requestedTrack2 = createTrackRepresentationFromVisibleTrack()
    val access = AccessParams.defaultAccess
  }

  "POST /playlists" >> {
    trait CreatePlaylistContext extends Context {
      val path = "/playlists"
      val isPublic = true
      val title = "title"
      val trackIds = Seq(Map("id" -> "1"))
      val testImage = "test-image.jpg"

      val buf = Await.result(
        BufReader.readAll(Reader.fromStream(this.getClass.getClassLoader.getResourceAsStream(testImage)))
      )
      val file =
        FileElement("playlist[artwork_data]", buf, Some("image/jpeg"), Some(testImage))
      val playlistCreate = PlaylistCreateOrUpdate(
        public = Value(isPublic),
        title = Value(title),
        tracks = Value(trackIds)
      )

      def stubService(
          playlistCreateOrUpdate: PlaylistCreateOrUpdate = playlistCreate,
          maybeArtworkRequest: Option[UpdatePlaylistArtworkRequest] = Some(UpdatePlaylistArtworkRequest(buf)),
          outcome: Outcome[Playlist] = Good(playlist)
      ) = {
        playlistsService.createPlaylist(session, playlistCreateOrUpdate, maybeArtworkRequest) returns outcome.outcomeF
      }
    }

    "Json request" >> {
      trait ValidPostBody {
        self: CreatePlaylistContext =>
        val playlistCreateBody = Json.stringify(
          Json.obj(
            "sharing" -> (if (isPublic) "public" else "private"),
            "title" -> title,
            "tracks" -> trackIds
          )
        )
      }

      trait InvalidPostBody {
        self: CreatePlaylistContext =>
        // missing all properties
        val playlistCreateBody = ""
      }

      trait Anonymous {
        self: CreatePlaylistContext =>
        // missing all properties
        override lazy val session: UserSession = anonymousSession
        val playlistCreateBody = ""
      }

      "returns 401 for anonymous user" in new CreatePlaylistContext with Anonymous {
        val response = post(path, Map.empty, Map("Host" -> "api.soundcloud.com"), playlistCreateBody)
        response.status ==== Status.Unauthorized
      }

      "with invalid POST body" >> {
        "returns 422" in new CreatePlaylistContext with InvalidPostBody {
          val response = post(path, Map.empty, Map("Host" -> "api.soundcloud.com"), playlistCreateBody)
          response.status ==== Status.UnprocessableEntity
        }
      }

      "with valid POST body" >> {
        "returns 201 when create succeeds" in new CreatePlaylistContext with ValidPostBody {
          stubService(maybeArtworkRequest = None)
          val response = post(path, Map.empty, Map("Host" -> "api.soundcloud.com"), playlistCreateBody)
          response.status ==== Status.Created
          response.contentString ==== Json.stringify(Json.toJson(playlist))
        }

        "on success adds the location header" in new CreatePlaylistContext with ValidPostBody {
          stubService(maybeArtworkRequest = None)
          val response = post(path, Map.empty, Map("Host" -> "api.soundcloud.com"), playlistCreateBody)
          response.headerMap("location") ==== s"$baseUrl/playlists/${playlist.id.toString}"
        }
      }
    }

    "Multipart/form request" >> {
      trait ValidPostBody {
        self: CreatePlaylistContext =>
        val playlistTitle = "my playlist"
        val playlistDescription = "description"
        val trackid1 = "3311"
        val trackid2 = "723290971"
        val trackid3 = "60521501"
        val playlistCreateBody = Seq[(String, String)](
          ("playlist[title]", playlistTitle),
          ("playlist[description]", playlistDescription),
          ("playlist[artwork_data]", testImage),
          ("playlist[tracks][][id]", trackid1),
          ("playlist[tracks][][id]", trackid2),
          ("playlist[tracks][][id]", trackid3)
        )

        val expectedTrackIds = Seq(Map("id" -> trackid1), Map("id" -> trackid2), Map("id" -> trackid3))
        val expectedPlaylistCreate = PlaylistCreateOrUpdate(
          title = Value(playlistTitle),
          description = Value(playlistDescription),
          tracks = Value(expectedTrackIds)
        )
      }

      trait Anonymous {
        self: CreatePlaylistContext =>
        // missing all properties
        override lazy val session: UserSession = anonymousSession
        val playlistCreateBody = Seq.empty
      }

      "returns 401 for anonymous user" in new CreatePlaylistContext with Anonymous {
        val response = postForm(path, body = playlistCreateBody, maybeFile = Some(file), isMultipart = true)
        response.status ==== Status.Unauthorized
      }

      "with valid POST body" >> {
        "returns 201 when create succeeds" in new CreatePlaylistContext with ValidPostBody {
          stubService(playlistCreateOrUpdate = expectedPlaylistCreate)
          val response = postForm(path, body = playlistCreateBody, maybeFile = Some(file), isMultipart = true)
          response.status ==== Status.Created
          response.contentString ==== Json.stringify(Json.toJson(playlist))
        }

        "extracts track ids" in new CreatePlaylistContext with ValidPostBody {
          stubService(playlistCreateOrUpdate = expectedPlaylistCreate)
          val response = postForm(path, body = playlistCreateBody, maybeFile = Some(file), isMultipart = true)
          response.status ==== Status.Created

          verify(playlistsService).createPlaylist(
            session,
            expectedPlaylistCreate,
            Some(UpdatePlaylistArtworkRequest(buf))
          )
        }

        "on success adds the location header" in new CreatePlaylistContext with ValidPostBody {
          stubService(playlistCreateOrUpdate = expectedPlaylistCreate)
          val response = postForm(path, body = playlistCreateBody, maybeFile = Some(file), isMultipart = true)
          response.headerMap("location") ==== s"$baseUrl/playlists/${playlist.id.toString}"
        }
      }
    }

    "application/x-www-form-urlencoded request" >> {
      trait ValidPostBody {
        self: CreatePlaylistContext =>
        val playlistTitle = "my playlist"
        val playlistDescription = "description"
        val trackid1 = "3311"
        val trackid2 = "723290971"
        val trackid3 = "60521501"
        val playlistCreateBody = Seq[(String, String)](
          ("playlist[title]", playlistTitle),
          ("playlist[description]", playlistDescription),
          ("playlist[tracks][][id]", trackid1),
          ("playlist[tracks][][id]", trackid2),
          ("playlist[tracks][][id]", trackid3)
        )

        val expectedTrackIds = Seq(Map("id" -> trackid1), Map("id" -> trackid2), Map("id" -> trackid3))
        val expectedPlaylistCreate = PlaylistCreateOrUpdate(
          title = Value(playlistTitle),
          description = Value(playlistDescription),
          tracks = Value(expectedTrackIds)
        )
      }

      trait Anonymous {
        self: CreatePlaylistContext =>
        // missing all properties
        override lazy val session: UserSession = anonymousSession
        val playlistCreateBody = Seq.empty
      }

      "returns 401 for anonymous user" in new CreatePlaylistContext with Anonymous {
        val response = postForm(path, body = playlistCreateBody)
        response.status ==== Status.Unauthorized
      }

      "with valid POST body" >> {
        "returns 201 when create succeeds" in new CreatePlaylistContext with ValidPostBody {
          stubService(playlistCreateOrUpdate = expectedPlaylistCreate, maybeArtworkRequest = None)
          val response = postForm(path, body = playlistCreateBody)
          response.status ==== Status.Created
          response.contentString ==== Json.stringify(Json.toJson(playlist))
        }

        "extracts track ids" in new CreatePlaylistContext with ValidPostBody {
          stubService(playlistCreateOrUpdate = expectedPlaylistCreate, maybeArtworkRequest = None)
          val response = postForm(path, body = playlistCreateBody)
          response.status ==== Status.Created

          verify(playlistsService).createPlaylist(
            session,
            expectedPlaylistCreate,
            None
          )
        }

        "on success adds the location header" in new CreatePlaylistContext with ValidPostBody {
          stubService(playlistCreateOrUpdate = expectedPlaylistCreate, maybeArtworkRequest = None)
          val response = postForm(path, body = playlistCreateBody)
          response.headerMap("location") ==== s"$baseUrl/playlists/${playlist.id.toString}"
        }
      }
    }

    "maps NotValid outcome from service to 422" in new CreatePlaylistContext {
      stubService(playlistCreateOrUpdate = nullPlaylist, maybeArtworkRequest = None, outcome = NotValid("").bad)
      val response = postForm(path, body = Seq.empty)
      response.status ==== Status.UnprocessableEntity
    }
  }

  "PUT /playlists" >> {
    trait UpdatePlaylistContext extends Context {
      val isPublic = true
      val title = "title"
      val trackIds = Seq(Map("id" -> "1"))

      val playlistCreateBody: String = ""
      val playlistCreate = PlaylistCreateOrUpdate(
        public = Value(isPublic),
        title = Value(title),
        tracks = Value(trackIds)
      )
      playlistsService.updatePlaylist(===(session), ===(playlistUrn), ===(playlistCreate), ===(None)) returns Good(
        playlist
      ).outcomeF

      lazy val response = put("/playlists/1", Map.empty, Map("Host" -> "api.soundcloud.com"), playlistCreateBody)
    }

    trait ValidPostBody {
      self: UpdatePlaylistContext =>
      override val playlistCreateBody = Json.stringify(
        Json.obj(
          "sharing" -> (if (isPublic) "public" else "private"),
          "title" -> title,
          "tracks" -> trackIds
        )
      )
    }

    trait InvalidPostBody {
      self: UpdatePlaylistContext =>
      // missing all properties
      override val playlistCreateBody = ""
    }

    trait Anonymous {
      self: UpdatePlaylistContext =>
      // missing all properties
      override lazy val session: UserSession = anonymousSession
    }

    "returns 401 for anonymous user" in new UpdatePlaylistContext with Anonymous {
      response.status ==== Status.Unauthorized
    }

    "with invalid POST body" >> {
      "returns 422" in new UpdatePlaylistContext with InvalidPostBody {
        response.status ==== Status.UnprocessableEntity
      }
    }

    "with valid POST body" >> {
      "returns 201 when create succeeds" in new UpdatePlaylistContext with ValidPostBody {
        response.status ==== Status.Ok
        response.contentString ==== Json.stringify(Json.toJson(playlist))
      }

      "returns 404 when service responds with NotFound" in new UpdatePlaylistContext with ValidPostBody {
        playlistsService.updatePlaylist(session, playlistUrn, playlistCreate, None) returns NotFound().bad.outcomeF

        response.status ==== Status.NotFound
      }

      "returns 403 when service responds with NotAuthorized" in new UpdatePlaylistContext with ValidPostBody {
        playlistsService.updatePlaylist(session, playlistUrn, playlistCreate, None) returns NotAuthorized().bad.outcomeF

        response.status ==== Status.Forbidden
      }

      "returns 500 when service responds with unhandled response" in new UpdatePlaylistContext with ValidPostBody {
        playlistsService.updatePlaylist(session, playlistUrn, playlistCreate, None) returns CustomError(
          "blah"
        ).bad.outcomeF

        response.status ==== Status.InternalServerError
      }
    }
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
      val path = "/playlists/1/tracks?linked_partitioning=true&limit=1&secret_token=s-3creT&access=playable,preview"
      val mockRequest = Request(path)
      mockRequest.host = "localhost"
      val pagination = OffsetBasedPagination.build(mockRequest, Seq("linked_partitioning", "secret_token", "access"))
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
