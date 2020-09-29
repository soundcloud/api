package com.soundcloud.publicApiStrangler.handler

import java.util.TimeZone

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.liebling.{LikeDeleted, LikeNotFound}
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationSpecContext
}
import com.soundcloud.publicApiStrangler.service.{
  LikesService,
  OkCreateResponse,
  OkCreatedCreateResponse,
  SpamBlockedCreateResponse
}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.joda.time.DateTimeZone
import org.mockito.Mockito.when
import org.specs2.mutable.BeforeAfter
import play.api.libs.json.Json

class LikesHandlerSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication: UserAuthentication

    val likesService = mock[LikesService]
    val telemetry = Telemetry.createIsolatedInstance

    val handler = new LikesHandler(userAuthentication, likesService)

    override def routingDefinitions = Routing.forLikesHandler(handler)
  }

  trait LoggedOutContext extends Context {
    lazy val userAuthentication = new FakeUserAuthentication(anonymousSession)
  }

  trait LoggedInContext extends Context {
    lazy val userAuthentication = new FakeUserAuthentication(session)
  }

  "Getting tracks" >> {
    trait TracksForUserContext extends Context {
      lazy val userAuthentication = new FakeUserAuthentication(session)

      val queryString =
        "?page_size=1&cursor=2&linked_partitioning=1"

      def paginationParams(path: String): CursorBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        CursorBasedPagination.build(mockRequest, Seq("linked_partitioning"))

      }

      def stubUserTrackLikeForUrn(
          user: Urn,
          trackUrn: Urn,
          trackRepresentation: Option[TrackRepresentation]
      ) = {
        when(likesService.userTrackLikeForUrn(session, user, trackUrn))
          .thenReturn(Future.value(trackRepresentation))
      }

      def stubUserTracksLikes(
          user: Urn,
          path: String,
          collection: Collection[TrackRepresentation]
      ) = {
        when(likesService.userTracksLikes(session, user, paginationParams(path)))
          .thenReturn(Future.value(collection))
      }
    }

    trait SuccessfulResponse extends TrackRepresentationSpecContext with TracksForUserContext {
      val trackRepresentation = createTrackRepresentation()
      val tracksCollection = Collection(List(trackRepresentation), None)
      val expectedResponse = Collection.getRepresentation(tracksCollection, true)
    }

    "GET /users/:userId/favorites/:trackId" >> {
      "with a successful response from tracks service" >> {
        "returns track" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/users/1/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, Some(trackRepresentation))

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== Json.toJson(trackRepresentation).toString()
        }

        "returns not found if empty" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/users/1/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, None)

          val response = get(path)
          response.status ==== Status.NotFound
          response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
        }
      }
    }

    "GET /me/favorites/:trackId" >> {
      "with a successful response from tracks service" >> {
        "returns track" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/me/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, Some(trackRepresentation))

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== Json.toJson(trackRepresentation).toString()
        }

        "returns not found if empty" in new TracksForUserContext with SuccessfulResponse {
          val user = Urn("soundcloud", "users", "1")
          val urn = Urn("soundcloud", "tracks", "48786981")
          val path = s"/me/favorites/48786981$queryString"

          stubUserTrackLikeForUrn(user, urn, None)

          val response = get(path)
          response.status ==== Status.NotFound
          response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
        }
      }
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

          stubUserTracksLikes(user, path, tracksCollection)

          val response = get(path)
          response.status ==== Status.Ok
          response.contentString ==== expectedResponse
        }
      }
    }
  }

  "Creating tracks" >> {

    "POST /me/favorites/:trackId" >> {

      trait PostTrackLikeContext extends Context with BeforeAfter {
        override def before: Any = {}
        override def after: Any = {}

        lazy val trackUrn = Urn("soundcloud", "tracks", "1")
        lazy val jsonBody = """{"json": "body"}"""
        lazy val response = post(s"/me/favorites/${trackUrn.identifier}", Map(), Map(), jsonBody)
      }

      "logged in" >> {
        trait LoggedInPostTrackLikeContext extends PostTrackLikeContext with LoggedInContext

        "when path contains a not liked URN" >> {
          trait NonLikedUrnContext extends LoggedInPostTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.createTrackLike(session, trackUrn))
                .thenReturn(Future.value(OkCreatedCreateResponse))
            }
          }

          "returns 201" in new NonLikedUrnContext {
            response.statusCode ==== 201
          }

          "renders correct body" in new NonLikedUrnContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "201 - Created")
          }
        }

        "when URN is already liked" >> {
          trait AlreadyLikedUrnContext extends LoggedInPostTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.createTrackLike(session, trackUrn))
                .thenReturn(Future.value(OkCreateResponse))
            }
          }

          "returns 200" in new AlreadyLikedUrnContext {
            response.statusCode ==== 200
          }

          "renders correct body" in new AlreadyLikedUrnContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "200 - OK")
          }
        }

        "when urn is invalid" >> {
          trait InvalidUrnPostTrackLikeContext extends PostTrackLikeContext with LoggedInContext {
            override lazy val trackUrn = Urn("soundcloud", "tracks", ":")
          }

          "returns 400" in new InvalidUrnPostTrackLikeContext {
            response.statusCode ==== 400
          }

          "renders correct body" in new InvalidUrnPostTrackLikeContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "400 - Bad Request")
          }
        }

        "when the request is spam blocked" >> {
          trait SpamPostTrackLikesContext extends LoggedInPostTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.createTrackLike(session, trackUrn)).thenReturn(Future.value(SpamBlockedCreateResponse))
            }
          }

          "returns 429" in new SpamPostTrackLikesContext {
            response.statusCode ==== 429
          }

          "renders correct body" in new SpamPostTrackLikesContext {
            Json.parse(response.contentString) ==== Json.obj("status" -> "429 - Too Many Requests")
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

  "Deleting tracks" >> {

    "DELETE /me/favorites/:trackId" >> {

      trait DeleteTrackLikeContext extends Context with BeforeAfter {
        override def before: Any = {}
        override def after: Any = {}

        lazy val trackUrn = Urn("soundcloud", "tracks", "1")
        lazy val jsonBody = """{"json": "body"}"""
        lazy val response = delete(s"/me/favorites/${trackUrn.identifier}", Map(), Map(), jsonBody)
      }

      "logged in" >> {
        trait LoggedInDeleteTrackLikeContext extends DeleteTrackLikeContext with LoggedInContext

        "when path contains a not liked URN" >> {
          trait NonLikedUrnContext extends LoggedInDeleteTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.deleteTrackLike(session, trackUrn))
                .thenReturn(Future.value(LikeNotFound))
            }
          }

          "returns 404" in new NonLikedUrnContext {
            response.statusCode ==== 404
          }

          "renders correct body" in new NonLikedUrnContext {
            response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
          }
        }

        "when URN is liked" >> {
          trait LikedUrnContext extends LoggedInDeleteTrackLikeContext {
            override def before: Any = {
              super.before
              when(likesService.deleteTrackLike(session, trackUrn))
                .thenReturn(Future.value(LikeDeleted))
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
            Json.parse(response.contentString) ==== Json.obj("status" -> "400 - Bad Request")
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
}
