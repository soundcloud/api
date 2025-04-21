package com.soundcloud.apipublic.handler.comments

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.comments.Comment
import com.soundcloud.apipublic.client.mothership.{RateLimitedError, TooManyRequests}
import com.soundcloud.apipublic.client.tracks.CreateTrackCommentUserHasSpamWarning
import com.soundcloud.apipublic.service.comments.CommentService
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.{ParamMap, Status}
import org.mockito.Mockito.verify
import org.specs2.mock.Mockito
import play.api.libs.json.{JsString, Json}

class CommentsHandlerSpec extends UnitSpecification with Mockito {
  trait CommentsHandlerContext extends HandlerSpecificationScope {

    val commentService = mock[CommentService]

    val validTrackId = 262857585
    val validTrackUrn = Urn("soundcloud", "tracks", validTrackId.toString())
    val validClientId = "clientid"
    val headers = Map[String, String]()
    val baseUrl = "https://api.example.com"
    val user = UserBuilder.user(1)
    val comment = Comment(
      Urn("soundcloud", "comments", "1"),
      "hi",
      "2014/05/01 12:47:49 +0000",
      None,
      validTrackUrn,
      UserBuilder.user(20)
    )

    val params = Map(
      "client_id" -> validClientId,
      "linked_partitioning" -> "1"
    )

    val session: UserSession = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    lazy val handler = new CommentsHandler(userAuthentication, commentService, baseUrl)
    override def routingDefinitions() = Routing.forCommentsHandler(handler)
  }

  "GET /tracks/:trackId/comments" >> {

    trait GetContext extends CommentsHandlerContext {
      def stubService(outcome: OutcomeF[Collection[Comment]]) = {
        commentService
          .fetchTracksComments(any, any, any, any)
          .returns(outcome)
      }
    }

    "when service returns Good" >> {
      "returns ok" in new GetContext {
        stubService(Collection(List[Comment](), None).goodF)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
      }

      "when linked_partitioning param present, returns a collection of Comments and nextHref" in new GetContext {
        val nextHref = "https://de.link.com"
        val collection = Collection(List(comment), Some(nextHref))
        stubService(collection.goodF)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
        val representation =
          Json.obj("collection" -> Json.toJson(collection.items), "next_href" -> nextHref)
        response.contentString ==== Json.stringify(representation)
      }

      "when linked_partitioning param present, does not render nextHref if it's null" in new GetContext {
        val collection = Collection(List(comment), None)
        stubService(collection.goodF)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
        val representation =
          Json.obj("collection" -> Json.toJson(collection.items))
        response.contentString ==== Json.stringify(representation)
      }

      "when linked_partitioning param absent, returns a flat array of Comments" in new GetContext {
        val collection = Collection(List(comment), None)
        stubService(collection.goodF)

        override val params = Map("client_id" -> validClientId)
        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
        response.contentString ==== Json.stringify(Json.toJson(collection.items))
      }

      "defaults limit to 200 if param not present" in new GetContext {
        stubService(Collection(List[Comment](), None).goodF)

        get(s"/tracks/${validTrackId}/comments", params, headers)

        val expectedLimit = 200
        verify(commentService).fetchTracksComments(
          session,
          validTrackUrn,
          OffsetBasedPagination(
            "https://api.example.com",
            s"/tracks/${validTrackId}/comments",
            ParamMap("linked_partitioning" -> "1", "client_id" -> validClientId),
            None,
            expectedLimit
          ),
          None
        )
      }

      "includes the secret_token in the request and pagination" in new GetContext {
        stubService(Collection(List[Comment](), None).goodF)

        override val params = Map(
          "client_id" -> validClientId,
          "secret_token" -> "abc"
        )

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok

        verify(commentService).fetchTracksComments(
          session,
          validTrackUrn,
          OffsetBasedPagination(
            "https://api.example.com",
            s"/tracks/${validTrackId}/comments",
            ParamMap(
              "client_id" -> validClientId,
              "secret_token" -> "abc"
            ),
            None,
            200
          ),
          Some("abc")
        )
      }

      "attaches a 10 minute cache control header" in new GetContext {
        stubService(Collection(List[Comment](), None).goodF)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)

        response.status ==== Status.Ok
        response.headerMap.getOrNull("Cache-Control") ==== "max-age=600, must-revalidate"
      }
    }

    "when service returns Bad" >> {
      "when service returns NotFound, returns not found" in new GetContext {
        stubService(NotFound().badF)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.NotFound
        response.contentString must contain("404 - Not Found")
      }

      "any other Bad, returns bad request" in new GetContext {
        stubService(CustomError("").badF)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.BadRequest
      }

      "does not attach a cache control header" in new GetContext {
        stubService(NotFound().badF)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)

        response.status ==== Status.NotFound
        response.headerMap.get("Cache-Control") ==== None
      }
    }
  }

  "POST /tracks/:track_id/comments" >> {

    trait PostContext extends CommentsHandlerContext {
      val body =
        """
          |{
          |  "comment": {
          |    "body": "comment body", 
          |    "timestamp": 5000
          |  }
          |}
          |""".stripMargin

      def stubService(outcome: Outcome[Comment]) = {
        commentService
          .createComment(any, any, any)
          .returns(outcome.outcomeF)
      }
    }

    "failure cases" >> {
      "returns a 401 for an anonymous user" in new PostContext {
        override val session = anonymousSession
        override val body = ""
        val response = post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)
        response.status ==== Status.Unauthorized
      }

      "no comment parameter returns a 422 and gives error message" in new PostContext {
        override val body = ""
        val response = post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)

        response.status ==== Status.UnprocessableEntity
        (Json.parse(response.contentString) \ "message").get === JsString("Parameter comment is missing.")
      }

      "no comment body parameter returns a 422 and gives error message" in new PostContext {
        override val body =
          """
            |{
            |  "comment": {
            |    "timestamp": 5000
            |  }
            |}
            |""".stripMargin

        val response = post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)

        response.status ==== Status.UnprocessableEntity
        (Json.parse(response.contentString) \ "message").get === JsString("Body can't be blank.")
      }

      "returns 429 and the spam warning urn from mothership when spamblocked" in new PostContext {
        val spamUrn = Urn("soundcloud", "spam-warnings", "42")
        val rateLimitError = RateLimitedError(spamUrn)
        val customError = CustomError(
          TooManyRequests,
          Some(CustomError(rateLimitError))
        ).bad
        stubService(customError)

        val response = post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)

        response.status ==== Status.TooManyRequests
        (Json.parse(response.contentString) \ "spam_warning_urn").get === JsString(spamUrn.toString)
      }

      "returns 429 and the spam warning urn from the new comments service when spamblocked" in new PostContext {
        val spamUrn = Urn("soundcloud", "spam-warnings", "42")

        val customError = CustomError(
          CreateTrackCommentUserHasSpamWarning(spamUrn)
        ).bad

        stubService(customError)

        val response = post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)

        response.status ==== Status.TooManyRequests
        (Json.parse(response.contentString) \ "spam_warning_urn").get === JsString(spamUrn.toString)
      }
    }

    "success cases" >> {
      "returns a 201 created on success" in new PostContext {
        stubService(comment.good)

        val response = post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)
        response.status ==== Status.Created
        response.contentString ==== Json.stringify(Json.toJson(comment))
      }

      "adds Location header" in new PostContext {
        stubService(comment.good)

        val response = post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)
        response.headerMap.get("Location") ==== Some("https://api.soundcloud.com/comments/1")
      }

      "accepts Int as timestamp" in new PostContext {
        stubService(comment.good)
        val timestamp = 5000
        override val body =
          s"""
            |{
            |  "comment": {
            |    "body": "comment body", 
            |    "timestamp": $timestamp
            |  }
            |}
            |""".stripMargin

        post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)

        verify(commentService)
          .createComment(session, CreateCommentParams(validTrackUrn, "comment body", Some(timestamp), None))
      }

      "accepts Float as timestamp" in new PostContext {
        stubService(comment.good)
        val timestamp = 5000.123
        override val body =
          s"""
             |{
             |  "comment": {
             |    "body": "comment body", 
             |    "timestamp": $timestamp
             |  }
             |}
             |""".stripMargin

        post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)

        verify(commentService)
          .createComment(session, CreateCommentParams(validTrackUrn, "comment body", Some(timestamp.toInt), None))
      }

      "accepts String as timestamp" in new PostContext {
        stubService(comment.good)
        val timestamp = "5000.123"
        override val body =
          s"""
             |{
             |  "comment": {
             |    "body": "comment body", 
             |    "timestamp": $timestamp
             |  }
             |}
             |""".stripMargin

        post(s"/tracks/${validTrackId}/comments", Map.empty, headers, body)

        verify(commentService)
          .createComment(
            session,
            CreateCommentParams(validTrackUrn, "comment body", Some(timestamp.toFloat.toInt), None)
          )
      }
    }
  }
}
