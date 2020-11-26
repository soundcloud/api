package com.soundcloud.publicApiStrangler.handler.comments

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.comments.Comment
import com.soundcloud.publicApiStrangler.handler.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.service.comments.CommentService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.users.UserBuilder
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.Future
import org.specs2.mock.Mockito
import org.mockito.Mockito.verify
import play.api.libs.json.Json

class CommentsHandlerSpec extends UnitSpecification with Mockito {
  trait CommentsHandlerContext extends HandlerSpecificationScope {

    val commentService = mock[CommentService]
    val mothershipDispatcher = mock[DispatchToMothershipHandler]
    val telemetry = mock[Telemetry]

    val validTrackId = 262857585
    val validTrackUrn = Urn("soundcloud", "tracks", validTrackId.toString())
    val validClientId = "clientid"
    val headers = Map("Host" -> "api.example.com")

    val user = UserBuilder.user(1)
    val comment = Comment(1, "hi", "2014/05/01 12:47:49 +0000", None, validTrackId, 20, UserBuilder.user(20))

    val params = Map(
      "client_id" -> validClientId,
      "linked_partitioning" -> "1"
    )

    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    def stubService(outcome: Outcome[Collection[Comment]]) = {
      commentService
        .fetchTracksComments(any, any, any)
        .returns(Future.value(outcome))
    }

    lazy val handler = new CommentsHandler(userAuthentication, commentService, mothershipDispatcher, telemetry)
    override def routingDefinitions() = Routing.forCommentsHandlerTests(handler)
  }

  "GET /tracks/:trackId/comments" >> {

    "when service returns Good" >> {
      "returns ok" in new CommentsHandlerContext {
        stubService(Collection(List[Comment](), None).good)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
      }

      "when linked_partitioning param present, returns a collection of Comments and nextHref" in new CommentsHandlerContext {
        val nextHref = "https://de.link.com"
        val collection = Collection(List(comment), Some(nextHref))
        stubService(collection.good)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
        val representation =
          Json.obj("collection" -> Json.toJson(collection.items), "next_href" -> nextHref)
        response.contentString ==== Json.stringify(representation)
      }

      "when linked_partitioning param present, does not render nextHref if it's null" in new CommentsHandlerContext {
        val collection = Collection(List(comment), None)
        stubService(collection.good)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
        val representation =
          Json.obj("collection" -> Json.toJson(collection.items))
        response.contentString ==== Json.stringify(representation)
      }

      "when linked_partitioning param absent, returns a flat array of Comments" in new CommentsHandlerContext {
        val collection = Collection(List(comment), None)
        stubService(collection.good)

        override val params = Map("client_id" -> validClientId)
        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok
        response.contentString ==== Json.stringify(Json.toJson(collection.items))
      }

      "defaults limit to 200 if param not present" in new CommentsHandlerContext {
        stubService(Collection(List[Comment](), None).good)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)

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
          )
        )
      }

      "includes the secret_token in the request and pagination" in new CommentsHandlerContext {
        stubService(Collection(List[Comment](), None).good)

        override val params = Map(
          "client_id" -> validClientId,
          "secret_token" -> "abc"
        )

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.Ok

        verify(commentService).fetchTracksComments(
          any,
          any,
          ===(
            OffsetBasedPagination(
              "https://api.example.com",
              s"/tracks/${validTrackId}/comments",
              ParamMap(
                "client_id" -> validClientId,
                "secret_token" -> "abc"
              ),
              None,
              200
            )
          )
        )
      }

      "attaches a 10 minute cache control header" in new CommentsHandlerContext {
        stubService(Collection(List[Comment](), None).good)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)

        response.status ==== Status.Ok
        response.headerMap.getOrNull("Cache-Control") ==== "max-age=600, must-revalidate"
      }
    }

    "when service returns Bad" >> {
      "when service returns NotFound, returns not found" in new CommentsHandlerContext {
        stubService(NotFound().bad)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.NotFound
        response.contentString ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
      }

      "any other Bad, returns bad request" in new CommentsHandlerContext {
        stubService(CustomError("").bad)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)
        response.status ==== Status.BadRequest
        response.contentString ==== "{}"
      }

      "does not attach a cache control header" in new CommentsHandlerContext {
        stubService(NotFound().bad)

        val response = get(s"/tracks/${validTrackId}/comments", params, headers)

        response.status ==== Status.NotFound
        response.headerMap.get("Cache-Control") ==== None
      }
    }
  }
}
