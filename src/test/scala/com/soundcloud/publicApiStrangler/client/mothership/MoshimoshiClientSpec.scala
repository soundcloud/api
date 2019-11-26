package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.{Headers, HeadersBuilder}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.mothership.request.representation._
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.client.mothership.response.representation._
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.{Method, Response, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsObject, Json, _}

class MoshimoshiClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    val addToPlaylistResponseMapper = mock[AddToPlaylistResponseMapper]
    val deleteFromPlaylistResponseMapper = mock[DeleteFromPlaylistResponseMapper]
    val createPlaylistResponseMapper = mock[CreatePlaylistResponseMapper]
    val updatePlaylistResponseMapper = mock[UpdatePlaylistResponseMapper]
    val deletePlaylistResponseMapper = mock[DeletePlaylistResponseMapper]
    val updateUserResponseMapper = mock[UpdateUserResponseMapper]
    val resetUserPasswordResponseMapper = mock[ResetUserPasswordResponseMapper]
    val deleteUserResponseMapper = mock[DeleteUserResponseMapper]
    implicit val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val client = new MoshimoshiClient(
      service,
      addToPlaylistResponseMapper,
      deleteFromPlaylistResponseMapper,
      createPlaylistResponseMapper,
      deletePlaylistResponseMapper,
      updatePlaylistResponseMapper,
      updateUserResponseMapper,
      resetUserPasswordResponseMapper,
      deleteUserResponseMapper
    )
  }

  def buildHeaders(entries: (String, String)*): Headers =
    entries.foldLeft(new HeadersBuilder()) { case (builder, (key, value)) => builder.set(key, value) }.build()

  // A recent play-json upgrade has introduced a change that does not preserve Map key ordering, thus breaking some
  // specs that rely on comparing JSON as as strings. Since this is limited to only a few specs in this class only,
  // we just re-parse the json and create a fresh object to ensure we have the correct ordering. Sorry.
  // See https://github.com/playframework/play-json/issues/236
  def fixTrackFixture(v: JsValue): JsValue = {
    val track = (v \ "track").as[JsObject]
    Json.obj("track" -> (track ++ Json.obj()))
  }

  "#trackPurchaseLinks" >> {
    trait TestContext extends Context {
      val urns =
        Set(Urn("soundcloud", "tracks", "1"), Urn("soundcloud", "tracks", "2"), Urn("soundcloud", "tracks", "3"))

      def path = Path() / "tracks" / "purchase_links"

      def fetch = Await.result(client.trackPurchaseLinks(session, urns))
    }

    "found response" >> {
      "returns all links" in new TestContext {
        expectOkResponse(path, okidokiTrackPurchaseLinks, urns.toList)

        fetch ==== okidokiTrackPurchaseLinks.as[List[JsObject]].map(_.as[TrackPurchaseLink])
      }
    }

    "empty response" in new TestContext {
      expectOkResponse(path, JsArray(), urns.toList)

      fetch ==== List()
    }

    "invalid response" in new TestContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }

  "#addTrackToPlaylist" >> {
    trait PlaylistContext extends Context {
      val playlistUrn = Urn("soundcloud", "playlists", "1")
      val trackUrn = Urn("soundcloud", "tracks", "1")
      val path = Path() / "playlists" / playlistUrn / "tracks" / trackUrn / "add_track"
      val response = mock[Response]
      val addToPlaylistResponse = OkAddToPlaylistResponse

      def addedTrackResult = Await.result(client.addTrackToPlaylist(session, playlistUrn, trackUrn))

      when(service.putWithSession(session, path, Params.empty, Headers.empty, None))
        .thenReturn(Future.value(response))
      when(addToPlaylistResponseMapper.apply(response))
        .thenReturn(addToPlaylistResponse)
    }

    "returns mapped response" in new PlaylistContext {
      addedTrackResult ==== addToPlaylistResponse
      there was one(service).putWithSession(session, path, Params.empty, Headers.empty, None)
      there was one(addToPlaylistResponseMapper).apply(response)
    }
  }

  "#deleteTrackFromPlaylist" >> {
    trait PlaylistContext extends Context {
      val playlistUrn = Urn("soundcloud", "playlists", "1")
      val trackUrn = Urn("soundcloud", "tracks", "1")
      val path = Path() / "playlists" / playlistUrn / "tracks" / trackUrn / "remove_track"
      val response = mock[Response]
      val deleteFromPlaylistResponse = OkDeleteFromPlaylistResponse

      def removedTrackResult = Await.result(client.deleteTrackFromPlaylist(session, playlistUrn, trackUrn))

      when(service.deleteWithSession(session, path, Params.empty, Headers.empty, None))
        .thenReturn(Future.value(response))
      when(deleteFromPlaylistResponseMapper.apply(response))
        .thenReturn(deleteFromPlaylistResponse)
    }

    "returns mapped response" in new PlaylistContext {
      removedTrackResult ==== OkDeleteFromPlaylistResponse
      there was one(service).deleteWithSession(session, path, Params.empty, Headers.empty, None)
      there was one(deleteFromPlaylistResponseMapper).apply(response)
    }
  }

  "#createPlaylist" >> {
    trait PlaylistContext extends Context {
      val path = Path() / "playlists"
      val playlistParams: CreatePlaylistParams = new CreatePlaylistParams("Awesome mix1")
      val jsonResponse = mock[Response]
      val playlist = mock[Playlist]

      def createPlaylistResult = Await.result(client.createPlaylist(session, playlistParams))

      when(
        service
          .postWithSession(session, path, Params("title" -> "Awesome mix1", "public" -> "true"), Headers.empty, None)
      ).thenReturn(Future.value(jsonResponse))
      when(createPlaylistResponseMapper.apply(jsonResponse))
        .thenReturn(playlist)
    }

    "returns expected playlist" in new PlaylistContext {
      createPlaylistResult ==== playlist
      there was one(service).postWithSession(
        session,
        path,
        Params("title" -> "Awesome mix1", "public" -> "true"),
        Headers.empty,
        None
      )
      there was one(createPlaylistResponseMapper).apply(jsonResponse)
    }
  }

  "#updatePlaylist" >> {
    trait PlaylistContext extends Context {
      val playlistUrn = Urn("soundcloud", "playlists", "1")
      val path = Path() / "playlists" / playlistUrn
      val playlistUpdate = PlaylistUpdate(Value("Updated Title Mix"), Value(true))
      val response = mock[Response]
      val updatePlaylistResponse = InvalidUrnUpdatePlaylistResponse
      val jsonBody = Json.obj("title" -> "Updated Title Mix", "public" -> true)

      def updatedPlaylistResult = Await.result(client.updatePlaylist(session, playlistUrn, playlistUpdate))

      when(service.putWithSession(session, path, Params.empty, Headers.empty, Some(Json.stringify(jsonBody))))
        .thenReturn(Future.value(response))
      when(updatePlaylistResponseMapper.apply(response))
        .thenReturn(updatePlaylistResponse)
    }

    "returns mapped response" in new PlaylistContext {
      updatedPlaylistResult ==== updatePlaylistResponse
      there was one(service).putWithSession(session, path, Params.empty, Headers.empty, Some(Json.stringify(jsonBody)))
      there was one(updatePlaylistResponseMapper).apply(response)
    }
  }

  "#deletePlaylist" >> {
    trait PlaylistContext extends Context {
      val playlistUrn = Urn("soundcloud", "playlists", "1")
      val path = Path() / "playlists" / playlistUrn
      val response = mock[Response]
      val deletePlaylistResponse = InvalidUrnDeletePlaylistResponse

      def deletedPlaylistResult = Await.result(client.deletePlaylist(session, playlistUrn))

      when(service.deleteWithSession(session, path, Params.empty, Headers.empty, None))
        .thenReturn(Future.value(response))
      when(deletePlaylistResponseMapper.apply(response))
        .thenReturn(deletePlaylistResponse)
    }

    "returns mapped response" in new PlaylistContext {
      deletedPlaylistResult ==== deletePlaylistResponse
      there was one(service).deleteWithSession(session, path, Params.empty, Headers.empty, None)
      there was one(deletePlaylistResponseMapper).apply(response)
    }
  }

  "#fetchUsers" >> {
    trait UsersContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "10419549"), Urn("soundcloud", "users", "123123123"))

      def path = Path() / "users" / "fetch"

      def fetch = Await.result(client.fetchUsers(session, urns))
    }

    "found response" >> {
      "gotta fetch'em all" in new UsersContext {
        expectOkResponse(path, moshiUsers, urns.toList)

        List(moshiUser, moshiUser2) ==== fetch
      }
    }

    "not found response" in new UsersContext {
      expectOkResponse(path, JsArray(), urns.toList)

      fetch ==== List()
    }

    "invalid response" in new UsersContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }

  "#fetchUserObjects" >> {
    trait UsersContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "10419549"), Urn("soundcloud", "users", "123123123"))

      def path = Path() / "users" / "fetch"

      def fetch = Await.result(client.fetchUserObjects(session, urns))
    }

    "found response" >> {
      "gotta fetch'em all" in new UsersContext {
        expectOkResponse(path, moshiUsers, urns.toList)

        fetch ==== List(UserMapper(moshiUser), UserMapper(moshiUser2))
      }
    }

    "not found response" in new UsersContext {
      expectOkResponse(path, JsArray(), urns.toList)

      fetch ==== List()
    }

    "invalid response" in new UsersContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }

  "#fetchPlaylistObjects" >> {
    trait PlaylistsContext extends Context {
      val urns = Set(Urn("soundcloud", "playlists", "10419549"), Urn("soundcloud", "playlists", "123123123"))

      def path = Path() / "playlists" / "fetch"

      def fetch = Await.result(client.fetchPlaylistObjects(session, urns))
    }

    "found response" >> {
      "gotta fetch'em all" in new PlaylistsContext {
        expectOkResponse(path, moshiPlaylists, urns.toList)

        fetch ==== List(PlaylistMapper(moshiPlaylist), PlaylistMapper(moshiPlaylist2))
      }
    }

    "not found response" in new PlaylistsContext {
      expectOkResponse(path, JsArray(), urns.toList)

      fetch ==== List()
    }

    "invalid response" in new PlaylistsContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }

  "#fetchTracks" >> {
    trait TracksContext extends Context {
      val urns = Set(Urn("soundcloud", "tracks", "32322284"), Urn("soundcloud", "tracks", "22322286"))

      def path = Path() / "tracks" / "fetch"

      def fetch = Await.result(client.fetchTracks(session, urns))
    }

    "found response" >> {
      "empty response" in new TracksContext {
        expectOkResponse(path, JsArray(), urns.toList)

        fetch ==== List()
      }

      "gotta fetch'em all" in new TracksContext {
        expectOkResponse(path, moshiTracks, urns.toList)

        val expected = moshiTracks.as[List[JsObject]].map(TrackMapper(_))

        fetch.zip(expected).foreach {
          case (actualTrack, expectedTrack) =>
            actualTrack.urn ==== expectedTrack.urn
            actualTrack.user_urn ==== expectedTrack.user_urn
            actualTrack.uri ==== expectedTrack.uri
        }
      }
    }

    "invalid response" in new TracksContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }

  "fetch individual track" >> {
    trait FetchTrackContext extends Context {
      val urn = Urn("soundcloud", "tracks", "174090825")

      def path = Path() / "tracks" / urn

      def fetch = Await.result(client.fetchTrack(session, urn))
    }

    "found response is a track" in new FetchTrackContext {
      expectOkResponse(path, moshiTrackMinimal, Params.empty, Headers.empty)
      fetch.get.urn ==== urn
    }

    trait NotFoundTrackContext extends Context {
      val urn = Urn("soundcloud", "tracks", "32322")

      def path = Path() / "tracks" / urn

      def fetch = Await.result(client.fetchTrack(session, urn))
    }

    "not found response is None" in new NotFoundTrackContext {
      expectNotFoundResponse(path, Params.empty, Headers.empty)
      fetch ==== None
    }
  }

  "fetch playlists" >> {
    trait PlaylistsContext extends Context {
      val urns = Set(Urn("soundcloud", "playlists", "10419549"), Urn("soundcloud", "playlists", "123123123"))

      def path = Path() / "playlists" / "fetch"

      def fetch = Await.result(client.fetchPlaylists(session, urns))
    }

    "found response" >> {
      "gotta fetch'em all" in new PlaylistsContext {
        expectOkResponse(path, moshiPlaylists, urns.toList)

        List(moshiPlaylist, moshiPlaylist2) ==== fetch
      }
    }

    "not found response" in new PlaylistsContext {
      expectOkResponse(path, JsArray(), urns.toList)

      fetch ==== List()
    }

    "invalid response" in new PlaylistsContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }

  "#fetchPlaylistTracks" >> {
    trait PlaylistTracksContext extends Context {
      val urn = Urn("soundcloud", "playlists", "10419549")

      def path = Path() / "playlists" / urn / "tracks"
    }

    "invalid response" >> {
      "throws exception" in new PlaylistTracksContext {
        expectInternalErrorResponse(path, Params("limit" -> "10", "offset" -> "0"))

        Await.result(client.fetchPlaylistTracks(session, urn, 10, 0)) must throwA[IllegalStateException]
      }
    }

    "ok response" >> {
      "returns the tracks of the given playlist" in new PlaylistTracksContext {
        expectOkResponse(path, moshiPlaylistTracks, Params("limit" -> "10", "offset" -> "20"))

        val actual = Await.result(client.fetchPlaylistTracks(session, urn, 10, 20))
        val expected = moshiPlaylistTracks.as[List[JsObject]].map(TrackMapper(_))

        actual must haveSize(4)

        actual.zip(expected).foreach {
          case (actualTrack, expectedTrack) =>
            actualTrack.urn ==== expectedTrack.urn
            actualTrack.user_urn ==== expectedTrack.user_urn
            actualTrack.uri ==== expectedTrack.uri
        }
      }
    }
  }

  "#fetchPlaylistTrackUrns" >> {
    trait PlaylistTrackUrnsContext extends Context {
      val playlistUrn = Urn("soundcloud", "playlists", "10419549")

      def path = Path() / "playlists" / playlistUrn / "tracks_with_pagination"

      def params = Params("representation_type" -> "id")
    }

    "invalid response" >> {
      "throws exception" in new PlaylistTrackUrnsContext {
        expectInternalErrorResponse(path, params)

        Await.result(client.fetchPlaylistTrackUrns(session, playlistUrn)) must throwA[IllegalStateException]
      }
    }

    "ok response" >> {
      "returns the tracks of the given playlist" in new PlaylistTrackUrnsContext {
        expectOkResponse(path, moshiPlaylistTrackUrns, params)

        Await.result(client.fetchPlaylistTrackUrns(session, playlistUrn)) ==== List(
          Urn("soundcloud", "tracks", "123"),
          Urn("soundcloud", "tracks", "456")
        )
      }
    }
  }

  "#fetchWebProfiles" >> {
    trait WebProfilesContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")

      def path = Path() / "users" / 1 / "web_profiles"
    }

    "invalid response" >> {
      "throws exception" in new WebProfilesContext {
        expectInternalErrorResponse(path)

        Await.result(client.fetchWebProfiles(session, userUrn)) must throwA[IllegalStateException]
      }
    }

    "ok response" >> {
      "returns the web profiles of the given user" in new WebProfilesContext {
        expectOkResponse(path, moshiWebProfiles)

        val expected = WebProfileMapper(moshiWebProfiles.as[List[JsObject]])

        Await.result(client.fetchWebProfiles(session, userUrn)) ==== expected
      }
    }
  }

  "#createTrack" >> {
    trait TrackCreateWithS3ArtworkContext extends Context {
      val trackCreate = TrackCreate(
        api_streamable = None,
        commentable = None,
        description = None,
        downloadable = None,
        embeddable = None,
        feedable = None,
        genre = None,
        label_name = None,
        license = None,
        published_at = MissingValue,
        original_filename = None,
        permalink = Some("Test Permalink"),
        purchase_title = None,
        purchase_url = None,
        release_date = None,
        reveal_comments = None,
        reveal_stats = None,
        sharing = None,
        tag_list = None,
        title = "Test Title",
        uid = None,
        artwork_from_s3 = Value(
          S3Artwork(
            bucket = Some("soundcloud-images"),
            filename = Some("231231234-original.jpg")
          )
        )
      )

      val urn = Urn("soundcloud", "tracks", "174090825")
      val path = Path() / "tracks"
      val expectedTrackJson = moshiTrackMinimal

      val headers = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8", "Content-Length" -> "546")
      val filteredHeaders = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")

      def createTrackResult = Await.result(client.createTrack(session, trackCreate, headers))

      val bodies = ExpectedBody(
        responseBody = moshiTrackMinimal,
        requestBody = Some(fixTrackFixture(moshiTrackCreateWithArtwork))
      )
    }

    "service returns 201" >> {
      "returns expected track" in new TrackCreateWithS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Post, filteredHeaders, Status(201), bodies)

        createTrackResult match {
          case Success(track, status) => track.urn ==== urn && status ==== Status.Created
          case _ => failure("did not return Success")
        }
      }

      "s3 artwork is present" in new TrackCreateWithS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Post, filteredHeaders, Status(201), bodies)

        (moshiTrackCreateWithArtwork \ "track" \ "artwork_from_s3" \ "bucket") match {
          case JsDefined(_: JsString) => ok
          case x => failure("Did not get an artwork with a bucket")
        }
      }
    }

    trait TrackCreateWithoutS3ArtworkContext extends Context {
      val trackCreate = TrackCreate(
        api_streamable = None,
        commentable = None,
        description = None,
        downloadable = None,
        embeddable = None,
        feedable = None,
        genre = None,
        label_name = None,
        license = None,
        published_at = MissingValue,
        original_filename = None,
        permalink = Some("Test Permalink"),
        purchase_title = None,
        purchase_url = None,
        release_date = None,
        reveal_comments = None,
        reveal_stats = None,
        sharing = None,
        tag_list = None,
        title = "Test Title",
        uid = None,
        artwork_from_s3 = MissingValue
      )

      val urn = Urn("soundcloud", "tracks", "174090825")
      val path = Path() / "tracks"
      val expectedTrackJson = moshiTrackMinimal
      val headers = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8", "Content-Length" -> "546")
      val filteredHeaders = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")

      def createTrackResult = Await.result(client.createTrack(session, trackCreate, headers))

      val bodies = ExpectedBody(
        responseBody = moshiTrackMinimal,
        requestBody = Some(fixTrackFixture(moshiTrackCreate))
      )
    }

    "service returns 201" >> {
      "returns expected track" in new TrackCreateWithoutS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Post, filteredHeaders, Status(201), bodies)

        createTrackResult match {
          case Success(track, status) => track.urn ==== urn && status ==== Status.Created
          case _ => failure("did not return Success")
        }
      }

      "s3 artwork is absent" in new TrackCreateWithS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Post, filteredHeaders, Status(201), bodies)

        (moshiTrackCreate \ "track" \ "artwork_from_s3") match {
          case expected: JsUndefined => ok
          case _ => failure("Got an artwork")
        }
      }

      "null s3 artwork is present" in new TrackCreateWithS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Post, filteredHeaders, Status(201), bodies)

        (moshiTrackCreateWithNullArtwork \ "track" \ "artwork_from_s3") match {
          case JsDefined(JsNull) => ok
          case _ => failure("Got a non-null artwork")
        }

        (moshiTrackCreate \ "track" \ "artwork_from_s3" \ "bucket") match {
          case _: JsUndefined => ok
          case _ => failure("Got a bucket")
        }
      }
    }

    "service returns 422" >> {
      "returns the parsed error messages" in new TrackCreateWithoutS3ArtworkContext {
        when(service.postWithSession(any[UserSession], any[Path], any[Params], any[Headers], any[Option[String]]))
          .thenReturn(Future(jsonResponse(Status(422), moshiErrors)))

        createTrackResult ==== UnprocessableEntity(
          Seq(Error(message = "has already been taken", subject = Some("permalink")))
        )
      }
    }

    "service returns something unexpected" >> {
      "throws exception" in new TrackCreateWithoutS3ArtworkContext {
        when(service.postWithSession(any[UserSession], any[Path], any[Params], any[Headers], any[Option[String]]))
          .thenReturn(Future(jsonResponse(Status(500), moshiErrors)))
        createTrackResult must throwAn[Exception]
      }
    }

    "forwards X-Forwarded-For and X-Real-IP" in new TrackCreateWithoutS3ArtworkContext {
      when(service.postWithSession(any[UserSession], any[Path], any[Params], any[Headers], any[Option[String]]))
        .thenReturn(Future(jsonResponse(Status(422), moshiErrors)))

      createTrackResult ==== UnprocessableEntity(
        Seq(Error(message = "has already been taken", subject = Some("permalink")))
      )

      verify(service).postWithSession(
        any[UserSession],
        any[Path],
        any[Params],
        ===(buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")),
        any[Option[String]]
      )
    }

    trait TrackCreateWithoutPermalink extends Context {
      val trackCreate = TrackCreate(
        api_streamable = None,
        commentable = None,
        description = None,
        downloadable = None,
        embeddable = None,
        feedable = None,
        genre = None,
        label_name = None,
        license = None,
        published_at = MissingValue,
        original_filename = None,
        permalink = None,
        purchase_title = None,
        purchase_url = None,
        release_date = None,
        reveal_comments = None,
        reveal_stats = None,
        sharing = None,
        tag_list = None,
        title = "Test Title",
        uid = None,
        artwork_from_s3 = MissingValue
      )

      val urn = Urn("soundcloud", "tracks", "174090825")
      val path = Path() / "tracks"
      val expectedTrackJson = moshiTrackMinimal
      val headers = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8", "Content-Length" -> "546")
      val filteredHeaders = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")

      def createTrackResult = Await.result(client.createTrack(session, trackCreate, headers))

      val bodies = ExpectedBody(
        responseBody = moshiTrackMinimal,
        requestBody = Some(fixTrackFixture(moshiTrackCreateWithoutPermalink))
      )
    }

    "service returns 201" >> {
      "returns expected track" in new TrackCreateWithoutPermalink {
        expectResponse(path, Params.empty, Method.Post, filteredHeaders, Status(201), bodies)

        createTrackResult match {
          case Success(track, status) => track.urn ==== urn && status ==== Status.Created
          case _ => failure("did not return Success")
        }

        (moshiTrackCreateWithoutPermalink \ "track" \ "permalink") match {
          case JsDefined(JsNull) => ok
          case _ => failure("Got a value in the permalink field")
        }
      }
    }
  }

  "#updateTrack" >> {
    trait TrackUpdateWithoutS3ArtworkContext extends Context {
      val trackUpdate = TrackUpdate(
        api_streamable = None,
        commentable = None,
        description = None,
        downloadable = None,
        embeddable = None,
        feedable = None,
        genre = None,
        label_name = None,
        license = None,
        published_at = MissingValue,
        permalink = "Test Permalink",
        purchase_title = None,
        purchase_url = None,
        release_date = None,
        reveal_comments = None,
        reveal_stats = None,
        sharing = None,
        tag_list = None,
        title = "Test Title",
        replacing_uid = None,
        replacing_original_filename = None,
        artwork_from_s3 = MissingValue
      )

      val urn = Urn("soundcloud", "tracks", "174090825")
      val path = Path() / "tracks" / urn
      val headers = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8", "Content-Length" -> "546")
      val filteredHeaders = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")

      def updateTrackResult = Await.result(client.updateTrack(session, urn, trackUpdate, headers))

      val bodies = ExpectedBody(
        responseBody = moshiTrackMinimal,
        requestBody = Some(fixTrackFixture(moshiTrackUpdate))
      )
    }

    "service returns 200" >> {
      "returns expected track" in new TrackUpdateWithoutS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Put, filteredHeaders, Status(200), bodies)

        updateTrackResult match {
          case Success(track, status) => track.urn ==== urn && status ==== Status.Ok
          case _ => failure("did not return Success")
        }
      }
    }

    "service returns 404" >> {
      "returns NotFound" in new TrackUpdateWithoutS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Put, filteredHeaders, Status(404), bodies)

        updateTrackResult ==== NotFound(Nil)
      }

      "s3 artwork is absent" in new TrackUpdateWithoutS3ArtworkContext {
        (moshiTrackUpdate \ "track" \ "artwork_from_s3") match {
          case expected: JsUndefined => ok
          case _ => failure("Got an artwork")
        }
      }

      "null s3 artwork is present" in new TrackUpdateWithoutS3ArtworkContext {
        (moshiTrackUpdateWithNullArtwork \ "track" \ "artwork_from_s3") match {
          case JsDefined(JsNull) => ok
          case _ => failure("Got a non-null artwork")
        }

        (moshiTrackUpdateWithNullArtwork \ "track" \ "artwork_from_s3" \ "bucket") match {
          case expected: JsUndefined => ok
          case _ => failure("Got a bucket")
        }
      }
    }

    "service returns 422" >> {
      "returns Errors" in new TrackUpdateWithoutS3ArtworkContext {
        when(service.putWithSession(any[UserSession], any[Path], any[Params], any[Headers], any[Option[String]]))
          .thenReturn(Future(jsonResponse(Status(422), moshiErrors)))

        updateTrackResult ==== UnprocessableEntity(
          Seq(Error(message = "has already been taken", subject = Some("permalink")))
        )
      }
    }

    trait TrackUpdateWithS3ArtworkContext extends Context {
      val trackUpdate = TrackUpdate(
        api_streamable = None,
        commentable = None,
        description = None,
        downloadable = None,
        embeddable = None,
        feedable = None,
        genre = None,
        label_name = None,
        license = None,
        published_at = MissingValue,
        permalink = "Test Permalink",
        purchase_title = None,
        purchase_url = None,
        release_date = None,
        reveal_comments = None,
        reveal_stats = None,
        sharing = None,
        tag_list = None,
        title = "Test Title",
        replacing_uid = None,
        replacing_original_filename = None,
        artwork_from_s3 = Value(
          S3Artwork(
            bucket = Some("soundcloud-images"),
            filename = Some("231231234-original.jpg")
          )
        )
      )

      val urn = Urn("soundcloud", "tracks", "174090825")
      val path = Path() / "tracks" / urn
      val headers = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8", "Content-Length" -> "546")
      val filteredHeaders = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")

      lazy val updateTrackResult = Await.result(client.updateTrack(session, urn, trackUpdate, headers))

      val bodies = ExpectedBody(
        responseBody = moshiTrackMinimal,
        requestBody = Some(fixTrackFixture(moshiTrackUpdateWithArtwork))
      )
    }

    "service returns 200" >> {
      "returns expected track" in new TrackUpdateWithS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Put, filteredHeaders, Status(200), bodies)

        updateTrackResult match {
          case Success(track, status) => track.urn ==== urn && status ==== Status.Ok
          case _ => failure("did not return Success")
        }
      }

      "s3 artwork is present" in new TrackUpdateWithoutS3ArtworkContext {
        (moshiTrackUpdateWithArtwork \ "track" \ "artwork_from_s3" \ "bucket") match {
          case JsDefined(expected: JsString) => ok
          case _ => failure("Did not get an artwork")
        }
      }
    }

    "service returns 404" >> {
      "returns NotFound" in new TrackUpdateWithS3ArtworkContext {
        expectResponse(path, Params.empty, Method.Put, filteredHeaders, Status(404), bodies)

        updateTrackResult ==== NotFound(Nil)
      }
    }

    "service returns 422" >> {
      "returns Errors" in new TrackUpdateWithS3ArtworkContext {
        when(service.putWithSession(any[UserSession], any[Path], any[Params], any[Headers], any[Option[String]]))
          .thenReturn(Future(jsonResponse(Status(422), moshiErrors)))

        updateTrackResult ==== UnprocessableEntity(
          Seq(Error(message = "has already been taken", subject = Some("permalink")))
        )
      }
    }

    "service returns something unexpected" >> {
      "throws exception" in new TrackUpdateWithS3ArtworkContext {
        when(service.putWithSession(any[UserSession], any[Path], any[Params], any[Headers], any[Option[String]]))
          .thenReturn(Future(jsonResponse(Status(500), moshiErrors)))
        updateTrackResult must throwAn[Exception]
      }
    }

    "forwards X-Forwarded-For and X-Real-IP" in new TrackUpdateWithS3ArtworkContext {
      expectResponse(path, Params.empty, Method.Put, filteredHeaders, Status(404), bodies)
      updateTrackResult ==== NotFound(Nil)

      verify(service).putWithSession(
        any[UserSession],
        any[Path],
        any[Params],
        ===(buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")),
        any[Option[String]]
      )
    }

    trait TrackUpdateWithPublishedAtContext extends Context {
      val trackUpdateWithPublishedAt = TrackUpdate(
        api_streamable = None,
        commentable = None,
        description = None,
        downloadable = None,
        embeddable = None,
        feedable = None,
        genre = None,
        label_name = None,
        license = None,
        published_at = Value("date"),
        permalink = "Test Permalink",
        purchase_title = None,
        purchase_url = None,
        release_date = None,
        reveal_comments = None,
        reveal_stats = None,
        sharing = None,
        tag_list = None,
        title = "Test Title",
        replacing_uid = None,
        replacing_original_filename = None,
        artwork_from_s3 = MissingValue
      )

      val urn = Urn("soundcloud", "tracks", "174090825")
      val path = Path() / "tracks" / urn
      val headers = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8", "Content-Length" -> "546")
      val filteredHeaders = buildHeaders("X-Forwarded-For" -> "8.8.8.8", "X-Real-IP" -> "8.8.8.8")

      lazy val updateTrackResult = Await.result(client.updateTrack(session, urn, trackUpdateWithPublishedAt, headers))

      val bodies = ExpectedBody(
        responseBody = moshiTrackMinimal,
        requestBody = Some(fixTrackFixture(moshiTrackUpdateWithPublishedAt))
      )
    }

    "properly serializes existing published_at field" in new TrackUpdateWithPublishedAtContext {
      expectResponse(path, Params.empty, Method.Put, filteredHeaders, Status(200), bodies)

      updateTrackResult match {
        case Success(track, status) => track.urn ==== urn && status ==== Status.Ok
        case _ => failure("did not return Success")
      }
    }
  }

  "#fetchTrackGeoblockings" >> {
    trait GeoblockingsContext extends Context {
      val trackUrn = Urn("soundcloud", "tracks", "1")

      def path = Path() / "tracks" / trackUrn / "geo_blockings"

      def fetch = Await.result(client.fetchTrackGeoblockings(session, trackUrn))
    }

    "found response" in new GeoblockingsContext {
      expectOkResponse(path, moshiTrackGeoblockings)

      fetch ==== Option(List("DE", "US"))
    }

    "null response" in new GeoblockingsContext {
      expectOkResponse(path, JsObject(Seq("geo_blockings" -> JsNull)))

      fetch ==== None
    }

    "not found response" in new GeoblockingsContext {
      expectNotFoundResponse(path)

      fetch ==== None
    }

    "invalid response" in new GeoblockingsContext {
      expectInternalErrorResponse(path)

      fetch must throwA[IllegalStateException]
    }
  }

  "#fetchFeatureStatus" >> {
    trait FeaturesContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val name = "scheduled_publishings"

      def path = Path() / "users" / userUrn / "features" / name / "status"

      def fetch = Await.result(client.fetchFeatureStatus(session, userUrn, name))
    }

    "found response" in new FeaturesContext {
      expectOkResponse(path, moshiFeatureActive)

      fetch ==== FeatureStatus("scheduled_publishings", true)
    }

    "not found response" in new FeaturesContext {
      expectNotFoundResponse(path)

      fetch ==== FeatureStatus("scheduled_publishings", false)
    }

    "invalid response" in new FeaturesContext {
      expectInternalErrorResponse(path)

      fetch must throwA[IllegalStateException]
    }
  }

  "#updateTrackGeoblockings" >> {
    trait GeoblockingsContext extends Context {
      val trackUrn = Urn("soundcloud", "tracks", "1")
      val geoblockingsUpdate = List("DE", "US")

      def path = Path() / "tracks" / trackUrn / "geo_blockings"

      def update = Await.result(client.updateTrackGeoblockings(session, trackUrn, Some(geoblockingsUpdate)))

      val bodies = ExpectedBody(
        responseBody = moshiTrackGeoblockings,
        requestBody = Some(moshiTrackGeoblockings)
      )
    }

    "success response" in new GeoblockingsContext {
      expectResponse(path, Params.empty, Method.Put, Headers.empty, Status.Ok, bodies)

      update ==== Option(List("DE", "US"))
    }

    "sends key=null when input is None" in new GeoblockingsContext {
      override def update = Await.result(client.updateTrackGeoblockings(session, trackUrn, None))

      override val bodies = ExpectedBody(
        responseBody = moshiTrackGeoblockingsNull,
        requestBody = Some(moshiTrackGeoblockingsNull)
      )

      expectResponse(path, Params.empty, Method.Put, Headers.empty, Status.Ok, bodies)

      update ==== None
    }

    "not found response" in new GeoblockingsContext {
      expectResponse(path, Params.empty, Method.Put, Headers.empty, Status.NotFound, bodies)

      update ==== None
    }

    "invalid response" in new GeoblockingsContext {
      expectResponse(path, Params.empty, Method.Put, Headers.empty, Status.InternalServerError, bodies)

      update must throwAn[IllegalStateException]
    }
  }

  "#updateUserFeedsSettings" >> {
    trait UpdateUserFeedsSettings extends Context {
      val userFeedsSettingsUpdate = UserFeedsSettings(
        custom_author_name = None,
        custom_feed_title = None,
        custom_rss_feed_url = None,
        default_tracks_feedable = true,
        feeds_enabled = Some(true),
        is_explicit = true,
        language = Some("en"),
        redirect_url = None,
        feed_category = Some(FeedCategory(Urn("soundcloud", "feed-categories", "3"), Some("Science"))),
        email = Some(FeedEmail(Urn("soundcloud", "emails", "123"), Some("filipe@example.com")))
      )

      val urn = Urn("soundcloud", "users", "1")
      val path = Path() / "users" / urn / "feeds_settings"

      def result = Await.result(client.updateUserFeedsSettings(session, urn, userFeedsSettingsUpdate))

      val bodies = ExpectedBody(
        responseBody = userFeedsSettings,
        requestBody = Some(userFeedsSettings)
      )
    }

    "service returns 200" >> {
      "returns updated settings" in new UpdateUserFeedsSettings {
        expectResponse(path, Params.empty, Method.Put, Headers.empty, Status(200), bodies)
        //        service.putWithSession(session, path, Params.empty, Headers.empty, bodies.requestBody.map(Json.stringify)) returns
        //          Future(JsonResponseBuilder().status(Status(200)).body(Json.stringify(bodies.responseBody)).build)

        result ==== userFeedsSettingsUpdate
      }
    }

    "service returns non-200" >> {
      "throws exception" in new UpdateUserFeedsSettings {
        expectResponse(path, Params.empty, Method.Put, Headers.empty, Status(500), bodies)

        result must throwAn[IllegalStateException]
      }
    }
  }

  "#fetchUserFeedsSettings" >> {
    trait FetchUserFeedsSettings extends Context {
      val userFeedsSettingsResult = UserFeedsSettings(
        custom_author_name = None,
        custom_feed_title = None,
        custom_rss_feed_url = None,
        default_tracks_feedable = true,
        feeds_enabled = Some(true),
        is_explicit = true,
        language = Some("en"),
        redirect_url = None,
        feed_category = Some(FeedCategory(Urn("soundcloud", "feed-categories", "3"), Some("Science"))),
        email = Some(FeedEmail(Urn("soundcloud", "emails", "123"), Some("filipe@example.com")))
      )

      val urn = Urn("soundcloud", "users", "1")
      val path = Path() / "users" / urn / "feeds_settings"

      def result = Await.result(client.fetchUserFeedsSettings(session, urn))
    }

    "service returns 200" >> {
      "returns settings" in new FetchUserFeedsSettings {
        expectOkResponse(path, userFeedsSettings)

        result ==== Some(userFeedsSettingsResult)
      }
    }

    "service returns 404" >> {
      "returns None" in new FetchUserFeedsSettings {
        expectNotFoundResponse(path)

        result ==== None
      }
    }

    "service returns non-200" >> {
      "throws exception" in new FetchUserFeedsSettings {
        expectInternalErrorResponse(path)

        result must throwAn[IllegalStateException]
      }
    }
  }

  "#updateUser" >> {
    trait UpdateUserContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val response = mock[Response]
      val updateUserResponse = OkUpdateUserResponse
      val userUpdate = UserUpdate(city = Value("some city"))
      val path = Path() / "users" / userUrn

      when(
        service
          .putWithSession(session, path, Params.empty, Headers.empty, Some(Json.stringify(Json.toJson(userUpdate))))
      ).thenReturn(Future.value(response))
      when(updateUserResponseMapper.apply(response))
        .thenReturn(updateUserResponse)
    }

    "service is called" in new UpdateUserContext {
      Await.result(client.updateUser(session, userUrn, userUpdate)) ==== updateUserResponse
      there was one(service).putWithSession(
        session,
        path,
        Params.empty,
        Headers.empty,
        Some(Json.stringify(Json.toJson(userUpdate)))
      )
      there was one(updateUserResponseMapper).apply(response)
    }
  }

  "#resetUserPassword (with email)" >> {
    trait ResetUserPasswordContext extends Context {
      val email = "some@email.com"
      val response = mock[Response]
      val resetUserPasswordResponse = OkResetUserPasswordResponse

      when(
        service
          .postWithSession(session, Path() / "users" / "password_reset", Params("email" -> email), Headers.empty, None)
      ).thenReturn(Future.value(response))
      when(resetUserPasswordResponseMapper.apply(response))
        .thenReturn(resetUserPasswordResponse)
    }

    "service returns mapped response" in new ResetUserPasswordContext {
      Await.result(client.resetUserPassword(session, email)) ==== resetUserPasswordResponse
      there was one(service).postWithSession(
        session,
        Path() / "users" / "password_reset",
        Params("email" -> email),
        Headers.empty,
        None
      )
      there was one(resetUserPasswordResponseMapper).apply(response)
    }
  }

  "#resetUserPassword (with user id)" >> {
    trait ResetUserPasswordContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val response = mock[Response]
      val resetUserPasswordResponse = OkResetUserPasswordResponse

      def performCall() = Await.result(client.resetUserPassword(session, userUrn))

      when(
        service.postWithSession(
          session,
          Path() / "users" / "password_reset",
          Params("user_id" -> userUrn.identifier),
          Headers.empty,
          None
        )
      ).thenReturn(Future.value(response))
      when(resetUserPasswordResponseMapper.apply(response))
        .thenReturn(resetUserPasswordResponse)
    }

    "service returns mapped response" in new ResetUserPasswordContext {
      performCall() ==== resetUserPasswordResponse
      there was one(service).postWithSession(
        session,
        Path() / "users" / "password_reset",
        Params("user_id" -> userUrn.identifier),
        Headers.empty,
        None
      )
      there was one(resetUserPasswordResponseMapper).apply(response)
    }
  }

  "#deleteUser" >> {
    trait DeleteUserContext extends Context {
      val response = mock[Response]
      val userUrn = Urn("soundcloud", "users", "2")
      val reason = "because"
      val deleteUserResponse = OkDeleteUserResponse

      when(
        service.postWithSession(
          session,
          Path() / "purgatory",
          Params("urn" -> userUrn, "actor_urn" -> session.getUser, "reason" -> reason),
          Headers.empty,
          None
        )
      ).thenReturn(Future.value(response))
      when(deleteUserResponseMapper.apply(response))
        .thenReturn(deleteUserResponse)
    }

    "service returns mapped response" in new DeleteUserContext {
      Await.result(client.deleteUser(session, userUrn, Some(reason))) ==== deleteUserResponse
      there was one(service).postWithSession(
        session,
        Path() / "purgatory",
        Params("urn" -> userUrn, "actor_urn" -> session.getUser, "reason" -> reason),
        Headers.empty,
        None
      )
      there was one(deleteUserResponseMapper).apply(response)
    }
  }

  "resends email confirmations" >> {
    trait ResendEmailConfirmationContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val emailUrn = Urn("soundcloud", "emails", "2")

      def expectStatusForPath(path: Path, status: Status) = {
        expectResponse(
          path,
          Params.empty,
          Method.Post,
          Headers.empty,
          status,
          ExpectedBody(JsNull, None)
        )
      }
    }

    "invalid response" in new ResendEmailConfirmationContext {
      val path = Path() / "users" / userUrn / "emails" / emailUrn / "confirmation"
      expectStatusForPath(path, Status.InternalServerError)
      Await.result(client.resendEmailConfirmation(session, userUrn, emailUrn)) must throwAn[IllegalStateException]
    }

    "success response" in new ResendEmailConfirmationContext {
      val path = Path() / "users" / userUrn / "emails" / emailUrn / "confirmation"
      expectStatusForPath(path, Status.ResetContent)
      Await.result(client.resendEmailConfirmation(session, userUrn, emailUrn))
    }

    "for all unconfirmed email addresses" in new ResendEmailConfirmationContext {
      val path = Path() / "users" / userUrn / "emails" / "unconfirmed" / "confirmation"
      expectStatusForPath(path, Status.ResetContent)
      Await.result(client.resendEmailConfirmationForAllUnconfirmedEmails(session, userUrn))
    }
  }
}
