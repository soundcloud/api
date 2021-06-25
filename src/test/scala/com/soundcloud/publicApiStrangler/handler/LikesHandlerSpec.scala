package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome.{
  GoodOps,
  HttpResponseFields,
  HttpServiceError,
  NotFound,
  NotValid,
  Outcome,
  UnexpectedError
}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationSpecContext
}
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.soundcloud.publicApiStrangler.service.{
  CreateLikeResponse,
  DeleteLikeResponse,
  LikesService,
  TrackLikersResponse
}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.twinagle.ErrorCode.ResourceExhausted
import com.soundcloud.twinagle.TwinagleException
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.joda.time.DateTimeZone
import org.mockito.Mockito.when
import org.specs2.mutable.BeforeAfter
import play.api.libs.json.{JsArray, JsString, Json}

import java.util.TimeZone

class LikesHandlerSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication: UserAuthentication

    val likesService = mock[LikesService]
    val userRepresentationService = mock[UserRepresentationsService]
    val telemetry = Telemetry.createIsolatedInstance

    val handler = new LikesHandler(userAuthentication, likesService, userRepresentationService)

    override def routingDefinitions = Routing.forLikesHandler(handler)
  }

  trait LoggedOutContext extends Context {
    lazy val userAuthentication = new FakeUserAuthentication(anonymousSession)
  }

  trait LoggedInContext extends Context {
    lazy val userAuthentication = new FakeUserAuthentication(session)
  }

  "Getting likes on tracks" >> {
    trait TracksForUserContext extends Context {
      lazy val userAuthentication = new FakeUserAuthentication(session)

      val queryString =
        "?page_size=1&cursor=2&linked_partitioning=1"

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        CursorBasedPagination.build(mockRequest, Seq("linked_partitioning"))

      }

      def stubUserTracksLikes(
          user: Urn,
          path: String,
          collection: Collection[TrackRepresentation],
          access: AccessParams = AccessParams.defaultAccess
      ) = {
        when(likesService.userTracksLikes(session, user, access, paginationParams(path)))
          .thenReturn(Future.value(collection))
      }
    }

    trait SuccessfulResponse extends TrackRepresentationSpecContext with TracksForUserContext {
      val trackRepresentation = createTrackRepresentationFromVisibleTrack()
      val tracksCollection = Collection(List(trackRepresentation), None)
      val expectedResponse = Collection.getRepresentation(tracksCollection, true)
    }

    "GET /users/:userId/likes/tracks" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/users/1/likes/tracks/$queryString"

          stubUserTracksLikes(user, path, tracksCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }
    }

    "GET /me/likes/tracks" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/me/likes/tracks/$queryString"

          stubUserTracksLikes(user, path, tracksCollection, AccessParams.explicitAccess)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }
    }
  }

  "Getting likers of a track" >> {
    trait TrackLikersContext extends Context {
      lazy val userAuthentication = new FakeUserAuthentication(session)

      val userUrn = Urn("soundcloud", "users", "123")
      val okidokiUser =
        Fixtures.okidokiUsersWithDeprecatedCounts.as[JsArray].value.last
      val userRepresentation = UserRepresentationMapper(okidokiUser)
      val users = List(userRepresentation)

      val trackUrn = Urn("soundcloud", "tracks", "456")

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        CursorBasedPagination.build(mockRequest, Seq("linked_partitioning"))
      }

      when(userRepresentationService.getUsers(session, Seq(userUrn)))
        .thenReturn(Future.value(users))
    }

    trait SuccessfulResponseContext extends TrackLikersContext {

      def stubTrackLikers(
          track: Urn,
          path: String
      ) = {
        when(likesService.trackLikers(session, track, paginationParams(path)))
          .thenReturn(Future.value(TrackLikersResponse(Seq(userUrn), None).good))
      }

      val usersCollection = Collection(List(userRepresentation), None)
      val expectedResponse = Collection.getRepresentation(usersCollection, false)
    }

    "GET /tracks/:trackId/favoriters" >> {
      "with a successful response from tracks and user representations services" >> {
        "returns users" in new SuccessfulResponseContext {
          val path = s"/tracks/456/favoriters"

          stubTrackLikers(trackUrn, path)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }

        "renders a collection and next href if there's linked_partitioning" in new SuccessfulResponseContext {
          override val expectedResponse = Collection.getRepresentation(usersCollection, true)
          val path = "/tracks/456/favoriters?linked_partitioning=1"

          stubTrackLikers(trackUrn, path)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }

      "when likes service fails" >> {
        trait FailureContext extends TrackLikersContext {
          val path = s"/tracks/456/favoriters"

          def stubTrackLikers(
              outcome: Outcome[TrackLikersResponse],
              track: Urn,
              path: String
          ) = {
            when(likesService.trackLikers(session, track, paginationParams(path)))
              .thenReturn(Future.value(outcome))
          }
        }

        "returns 404 for Not Found" in new FailureContext {
          stubTrackLikers(NotFound().bad, trackUrn, path)
          val response = get(path)
          response.status ==== Status.NotFound
        }

        "returns 500 for unhandled errors" in new FailureContext {
          stubTrackLikers(NotValid("").bad, trackUrn, path)
          val response = get(path)
          response.status ==== Status.InternalServerError
        }
      }
    }
  }

  "Liking tracks" >> {

    "POST /likes/tracks/:trackId" >> {

      trait PostTrackLikeContext extends Context with BeforeAfter {
        override def before: Any = {}
        override def after: Any = {}

        lazy val trackUrn = Urn("soundcloud", "tracks", "1")
        lazy val jsonBody = """{"json": "body"}"""
        lazy val response = post(s"/likes/tracks/${trackUrn.identifier}", Map(), Map(), jsonBody)
      }

      "logged in" >> {
        trait LoggedInPostTrackLikeContext extends PostTrackLikeContext with LoggedInContext

        "when URN is valid" >> {
          trait AlreadyLikedUrnContext extends LoggedInPostTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.createTrackLike(session, trackUrn))
                .thenReturn(Future.value(CreateLikeResponse().good))
            }
          }

          "returns 200" in new AlreadyLikedUrnContext {
            response.statusCode ==== 200
          }

          "renders correct body" in new AlreadyLikedUrnContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "200 - OK")
          }
        }

        "when URN is invalid" >> {
          trait InvalidUrnPostTrackLikeContext extends PostTrackLikeContext with LoggedInContext {
            override lazy val trackUrn = Urn("soundcloud", "tracks", ":")
          }

          "returns 400" in new InvalidUrnPostTrackLikeContext {
            response.statusCode ==== 400
          }

          "renders correct body" in new InvalidUrnPostTrackLikeContext {
            (Json.parse(response.contentString) \ "status").get ==== JsString("400 - Bad Request")
          }
        }

        "when the request is spam blocked" >> {
          trait SpamPostTrackLikesContext extends LoggedInPostTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.createTrackLike(session, trackUrn))
                .thenReturn(Future.value(UnexpectedError(TwinagleException(ResourceExhausted, "")).bad))
            }
          }

          "returns 429" in new SpamPostTrackLikesContext {
            response.statusCode ==== 429
          }

          "renders correct body" in new SpamPostTrackLikesContext {
            (Json.parse(response.contentString) \ "status").get ==== JsString("429 - Too Many Requests")
          }
        }
      }

      "logged out" >> {
        "returns 401" in new PostTrackLikeContext with LoggedOutContext {
          response.statusCode ==== 401
        }
      }
    }
  }

  "Unliking tracks" >> {

    "DELETE /likes/tracks/:trackId" >> {

      trait DeleteTrackLikeContext extends Context with BeforeAfter {
        override def before: Any = {}
        override def after: Any = {}

        lazy val trackUrn = Urn("soundcloud", "tracks", "1")
        lazy val jsonBody = """{"json": "body"}"""
        lazy val response = delete(s"/likes/tracks/${trackUrn.identifier}", Map(), Map(), jsonBody)
      }

      "logged in" >> {
        trait LoggedInDeleteTrackLikeContext extends DeleteTrackLikeContext with LoggedInContext

        "when path contains a not liked URN" >> {
          trait NonLikedUrnContext extends LoggedInDeleteTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.deleteTrackLike(session, trackUrn))
                .thenReturn(Future.value(NotFound().bad))
            }
          }

          "returns 404" in new NonLikedUrnContext {
            response.statusCode ==== 404
          }
        }

        "when URN is liked" >> {
          trait LikedUrnContext extends LoggedInDeleteTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.deleteTrackLike(session, trackUrn))
                .thenReturn(Future.value(DeleteLikeResponse().good))
            }
          }

          "returns 200" in new LikedUrnContext {
            response.statusCode ==== 200
          }

          "renders correct body" in new LikedUrnContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "200 - OK")
          }
        }

        "when urn is invalid" >> {
          trait InvalidUrnDeleteTrackLikeContext extends DeleteTrackLikeContext with LoggedInContext {
            override lazy val trackUrn = Urn("soundcloud", "tracks", ":")
          }

          "returns 400" in new InvalidUrnDeleteTrackLikeContext {
            response.statusCode ==== 400
          }

          "renders correct body" in new InvalidUrnDeleteTrackLikeContext {
            (Json.parse(response.contentString) \ "status").get ==== JsString("400 - Bad Request")
          }
        }
      }

      "logged out" >> {
        "returns 401" in new DeleteTrackLikeContext with LoggedOutContext {
          response.statusCode ==== 401
        }
      }
    }
  }

  "Liking playlists" >> {

    "POST /likes/playlists/:playlistId" >> {

      trait PostPlaylistLikeContext extends Context with BeforeAfter {
        override def before: Any = {}
        override def after: Any = {}

        lazy val playlistUrn = Urn("soundcloud", "playlists", "200")
        lazy val jsonBody = """{"json": "body"}"""
        lazy val response = post(s"/likes/playlists/${playlistUrn.identifier}", Map(), Map(), jsonBody)
      }

      "logged in" >> {
        trait LoggedInPostPlaylistLikeContext extends PostPlaylistLikeContext with LoggedInContext

        "like a playlist" >> {
          trait LikedContext extends LoggedInPostPlaylistLikeContext {
            override def before: Any = {
              super.before
              when(likesService.createPlaylistLike(session, playlistUrn))
                .thenReturn(Future.value(CreateLikeResponse().good))
            }
          }

          "returns 200" in new LikedContext {
            response.statusCode ==== 200
          }

          "renders correct body" in new LikedContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "200 - OK")
          }
        }

        "when urn is invalid" >> {
          trait InvalidUrnPostPlaylistLikeContext extends PostPlaylistLikeContext with LoggedInContext {
            override lazy val playlistUrn = Urn("soundcloud", "playlists", ":")
          }

          "returns 400" in new InvalidUrnPostPlaylistLikeContext {
            response.statusCode ==== 400
          }

          "renders correct body" in new InvalidUrnPostPlaylistLikeContext {
            (Json.parse(response.contentString) \ "status").get ==== JsString("400 - Bad Request")
          }
        }

        "when the request is spam blocked" >> {
          trait SpamPostPlaylistLikesContext extends LoggedInPostPlaylistLikeContext {
            override def before: Any = {
              super.before
              when(likesService.createPlaylistLike(session, playlistUrn))
                .thenReturn(Future.value(HttpServiceError(HttpResponseFields(Status.TooManyRequests.code)).bad))
            }
          }

          "returns 429" in new SpamPostPlaylistLikesContext {
            response.statusCode ==== 429
          }

          "renders correct body" in new SpamPostPlaylistLikesContext {
            (Json.parse(response.contentString) \ "status").get ==== JsString("429 - Too Many Requests")
          }
        }
      }

      "logged out" >> {
        "returns 401" in new PostPlaylistLikeContext with LoggedOutContext {
          response.statusCode ==== 401
        }
      }
    }
  }

  "Unliking playlists" >> {

    "DELETE /likes/playlists/:playlistId" >> {

      trait DeletePlaylistLikeContext extends Context with BeforeAfter {
        override def before: Any = {}
        override def after: Any = {}

        lazy val playlistUrn = Urn("soundcloud", "playlists", "1")
        lazy val jsonBody = """{"json": "body"}"""
        lazy val response = delete(s"/likes/playlists/${playlistUrn.identifier}", Map(), Map(), jsonBody)
      }

      "logged in" >> {
        trait LoggedInDeletePlaylistLikeContext extends DeletePlaylistLikeContext with LoggedInContext

        "when path contains a not liked URN" >> {
          trait NonLikedUrnContext extends LoggedInDeletePlaylistLikeContext {
            override def before: Any = {
              super.before
              when(likesService.deletePlaylistLike(session, playlistUrn))
                .thenReturn(Future.value(NotFound().bad))
            }
          }

          "returns 404" in new NonLikedUrnContext {
            response.statusCode ==== 404
          }
        }

        "when URN is liked" >> {
          trait LikedUrnContext extends LoggedInDeletePlaylistLikeContext {
            override def before: Any = {
              super.before
              when(likesService.deletePlaylistLike(session, playlistUrn))
                .thenReturn(Future.value(DeleteLikeResponse().good))
            }
          }

          "returns 200" in new LikedUrnContext {
            response.statusCode ==== 200
          }

          "renders correct body" in new LikedUrnContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "200 - OK")
          }
        }

        "when urn is invalid" >> {
          trait InvalidUrnDeletePlaylistLikeContext extends DeletePlaylistLikeContext with LoggedInContext {
            override lazy val playlistUrn = Urn("soundcloud", "playlists", ":")
          }

          "returns 400" in new InvalidUrnDeletePlaylistLikeContext {
            response.statusCode ==== 400
          }

          "renders correct body" in new InvalidUrnDeletePlaylistLikeContext {
            (Json.parse(response.contentString) \ "status").get ==== JsString("400 - Bad Request")
          }
        }
      }

      "logged out" >> {
        "returns 401" in new DeletePlaylistLikeContext with LoggedOutContext {
          response.statusCode ==== 401
        }
      }
    }
  }

  // To be deprecated in favour ir new route names
  "Getting likes on tracks" >> {
    trait TracksForUserContext extends Context {
      lazy val userAuthentication = new FakeUserAuthentication(session)

      val queryString =
        "?page_size=1&cursor=2&linked_partitioning=1"

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        CursorBasedPagination.build(mockRequest, Seq("linked_partitioning"))

      }

      def stubUserTracksLikes(
          user: Urn,
          path: String,
          collection: Collection[TrackRepresentation],
          access: AccessParams = AccessParams.defaultAccess
      ) = {
        when(likesService.userTracksLikes(session, user, access, paginationParams(path)))
          .thenReturn(Future.value(collection))
      }
    }

    trait SuccessfulResponse extends TrackRepresentationSpecContext with TracksForUserContext {
      val trackRepresentation = createTrackRepresentationFromVisibleTrack()
      val tracksCollection = Collection(List(trackRepresentation), None)
      val expectedResponse = Collection.getRepresentation(tracksCollection, true)
    }

    "GET /users/:userId/favorites" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/users/1/favorites/$queryString"

          stubUserTracksLikes(user, path, tracksCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }
    }

    "GET /me/favorites" >> {
      "with a successful response from tracks service" >> {
        "returns tracks" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val path = s"/me/favorites/$queryString"

          stubUserTracksLikes(user, path, tracksCollection, AccessParams.explicitAccess)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }
    }
  }
}
