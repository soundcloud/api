package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{CustomError, NotAllowed, NotValid}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.chrono.ChronoResponse
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.handler.comments.CreateCommentParams
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.{ParamMap, Response, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json._
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.moshimoshicomments.MoshimoshiCommentsComment
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.WebProfile
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException

class MoshimoshiClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    val exceptionCollector = mock[ExceptionCollector]

    implicit val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val client = new MoshimoshiClient(
      service,
      exceptionCollector
    )
  }

  "#createComment" >> {
    trait CreateCommentContext extends Context {
      val path = Path() / "comments"
      val trackUrn = Urn("soundcloud", "tracks", "2")

      val createCommentParams = CreateCommentParams(trackUrn, "body", Some(1000), None)
      val serviceParams = Params(
        "track_id" -> createCommentParams.trackUrn.identifier,
        "comment[body]" -> createCommentParams.body,
        "comment[timestamp]" -> "1000"
      )

      val moshimoshiComment = Fixtures.okidokiComment
    }

    "201 response" in new CreateCommentContext {
      when(service.postWithSession(session, path, serviceParams, Headers.empty, None))
        .thenReturn(Future.value(jsonResponse(Status.Created, moshimoshiComment)))

      Await.result(client.createComment(session, createCommentParams)) ==== moshimoshiComment
        .as[MoshimoshiCommentsComment]
        .good
    }

    "422 response" in new CreateCommentContext {
      when(service.postWithSession(session, path, serviceParams, Headers.empty, None))
        .thenReturn(Future.value(jsonResponse(Status.UnprocessableEntity, JsNull)))

      Await.result(client.createComment(session, createCommentParams)) ==== CustomError(UnprocessableEntity).bad
    }

    "403 response" in new CreateCommentContext {
      when(service.postWithSession(session, path, serviceParams, Headers.empty, None))
        .thenReturn(Future.value(jsonResponse(Status.Forbidden, JsNull)))

      Await.result(client.createComment(session, createCommentParams)) ==== NotAllowed().bad
    }

    "429 response" in new CreateCommentContext {
      val spamWarning = Fixtures.okidokiSpamWarning
      when(service.postWithSession(session, path, serviceParams, Headers.empty, None))
        .thenReturn(Future.value(jsonResponse(Status.TooManyRequests, spamWarning)))

      val expectedError = CustomError(
        TooManyRequests,
        Some(CustomError(RateLimitedError(Urn("soundcloud", "spam-warnings", "1"))))
      )

      Await.result(client.createComment(session, createCommentParams)) ==== expectedError.bad
    }

    "Unhandled response returns NotValid" in new CreateCommentContext {
      when(service.postWithSession(session, path, serviceParams, Headers.empty, None))
        .thenReturn(Future.value(jsonResponse(Status.RequestEntityTooLarge, JsNull)))

      Await.result(client.createComment(session, createCommentParams)) ==== NotValid("Something went wrong").bad
    }
  }

  "#fetchUserObjects" >> {
    trait UsersContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "10419549"), Urn("soundcloud", "users", "123123123"))

      def path = Path() / "users" / "fetch"

      def fetch = Await.result(client.fetchUserObjects(session, urns))
    }

    "found response" in new UsersContext {
      expectOkResponse(path, moshiUsers, urns.toList)

      fetch ==== List(UserRepresentationMapper(moshiUser), UserRepresentationMapper(moshiUser2))
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

  "#userPlaylists" >> {
    trait PlaylistsByUser extends Context {
      val userUrn = Urn("soundcloud", "users", "1")

      val path = Path("/users") / userUrn / "playlists" / "chrono"
      val pagination = CursorBasedPagination(
        "https://api.soundcloud.com",
        "/users/1/playlists/",
        ParamMap(),
        Some("2"),
        2
      )
    }

    "200 status" in new PlaylistsByUser {
      when(
        service.getWithSession(
          anonymousSession,
          path,
          Params("cursor" -> "2", "limit" -> "2", "direction" -> "desc"),
          Headers.empty
        )
      ).thenReturn(Future(jsonResponse(Status.Ok, moshimoshiPlaylistsChrono)))

      val result = Await.result(client.userPlaylists(anonymousSession, userUrn, pagination))
      result.items must haveSize(2)
    }

    "500 status" in new PlaylistsByUser {
      when(
        service.getWithSession(
          anonymousSession,
          path,
          Params("cursor" -> "2", "limit" -> "2", "direction" -> "desc"),
          Headers.empty
        )
      ).thenReturn(Future(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(client.userPlaylists(anonymousSession, userUrn, pagination)) ==== ChronoResponse.emptyResponse
    }
  }

  "#userWebProfiles" >> {
    trait WebProfilesContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")

      def path = Path("/users") / userUrn.identifier / "web_profiles"

      def fetch = Await.result(client.userWebProfiles(session, userUrn))
    }

    "found response" in new WebProfilesContext {
      expectOkResponse(path, webProfiles)

      val expectedWebProfiles = webProfiles.as[List[WebProfile]]

      fetch ==== expectedWebProfiles
    }

    "invalid response" in new WebProfilesContext {
      expectInternalErrorResponse(path)

      fetch must throwA[IllegalStateException]
    }
  }

  "#updatePlaylistArtwork" >> {
    trait UpdatePlaylistContext extends Context {
      val playlistUrn = Urn("soundcloud", "playlists", "1")
      val path = Path() / "playlists" / playlistUrn / "artwork"
      val playlistArtworkParams = PlaylistArtworkUpdate(bucket = "bucket1", filename = "myimage")
      val jsonBody = Json.obj("bucket" -> "bucket1", "filename" -> "myimage")
      def mockService(response: Future[Response]) = {
        when(
          service
            .putWithSession(session, path, Params.empty, Headers.empty(), Some(Json.stringify(jsonBody)))
        ).thenReturn(response)
      }
      def result = {
        mockService(Future.value(Response(status = Status.Created)))

        Await.result(client.updatePlaylistArtwork(session, playlistUrn, playlistArtworkParams).value)
      }
    }
    "happy path" >> {
      trait HappyPathContext extends UpdatePlaylistContext
      "it returns mapped response" in new HappyPathContext {
        result must beEqualTo(Good(()))
      }
    }

    "Response mapper cannot map response" >> {
      trait UnMappedResponseContext extends UpdatePlaylistContext {
        val unexpectedResponse = Response(status = Status.Accepted)
        override def result = {
          mockService(Future.value(unexpectedResponse))

          Await.result(client.updatePlaylistArtwork(session, playlistUrn, playlistArtworkParams).value)
        }
      }

      "it throws an unhandled response error " in new UnMappedResponseContext {
        result must throwA[UnhandledResponseException]
      }
    }

  }

}
