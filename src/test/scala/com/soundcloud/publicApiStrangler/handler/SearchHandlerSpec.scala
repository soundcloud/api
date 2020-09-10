package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.bff.nextbff.test.JsonMappingMock
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.search.{Search, SearchDispatcherRequest, SearchMapper}
import com.soundcloud.publicApiStrangler.service.SearchService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.{
  PlaylistRepresentationSpecContext,
  Collection
}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackPagination, TrackRepresentationSpecContext}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.Json

class SearchHandlerSpec extends UnitSpecification {
  trait ForwardContext extends HandlerSpecificationScope {
    def followCountsSeq: Seq[FollowCounts] = Seq.empty

    val fallbackMock = mock[DispatchToMothershipHandler]
    val searchMapperMock = mock[SearchMapper]
    val followCountsClientMock = mock[FollowCountsClient]
    val lieblingClientMock = mock[LieblingClient]
    val repostsClientMock = mock[RepostsClient]
    val exceptionCollector = new ExceptionCollector(Telemetry.createIsolatedInstance)
    val searchService = mock[SearchService]

    val authentication = new FakeUserAuthentication(anonymousSession)
    val userRelatedMothershipDispatcher = new UserRelatedMothershipDispatcher(
      authentication,
      fallbackMock,
      followCountsClientMock,
      lieblingClientMock,
      () => Future.value(true),
      repostsClientMock
    )

    val handler = new SearchHandler(
      authentication,
      searchMapperMock,
      "http://api.soundcloud.com",
      userRelatedMothershipDispatcher,
      searchService
    )

    override def routingDefinitions = Routing.forSearchHandler(handler)

    def doesNotForward(response: Response) = {
      there was noCallsTo(fallbackMock)
    }
  }

  "when resource that supports search is called" >> {
    trait Context extends ForwardContext {
      val endpoints = Seq(
        ("/users", SearchDispatcherRequest.userSearch, handler.dispatchUserRequest)
      )

      val queryParams = Map("q" -> "foo")
      val pageParams = Map("offset" -> "0", "limit" -> "10")
      val extraParams = pageParams ++ queryParams
      val headers = Map.empty[String, String]

      abstract class SearchMock extends JsonMappingMock with Search

      val searchMock = JsonMappingMock.prepare[SearchMock]
    }

    "forwards to Mothership when q param not present" in new Context {
      // just so we can distinguish a forwarded request. Typically, this would be 200.
      val forwardStatus = Status.Found
      val forwardContent = "forwardContent"

      def expectForwardedRequest = {
        val response = JsonResponseBuilder().body(forwardContent).status(forwardStatus).build
        fallbackMock.dispatch(any[HandlerRequest]) returns Future.value(response)

        fallbackMock
          .dispatch(any[HandlerRequest])
          .returns(Future(response))

        followCountsClientMock.counts(any[UserSession], any[Seq[Urn]]) returns Future.value(followCountsSeq)
      }

      def stillForwards(response: Response) = {
        response.status ==== forwardStatus
        response.contentString ==== "forwardContent"
      }

      endpoints.foreach {
        case (apiEndPoint, dispatcherRequest, handler) =>
          expectForwardedRequest
          val response = get(apiEndPoint)
          stillForwards(response)
      }
    }

    "performs a search when q param is present" in new Context {
      endpoints.foreach {
        case (apiEndPoint, dispatcherRequest, handler) =>
          val request = com.twitter.finagle.http.Request(apiEndPoint, extraParams.toSeq: _*)
          val query = dispatcherRequest(request)
          val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, extraParams, 0, 10)

          when(searchMapperMock.materialize(anonymousSession, page))
            .thenReturn(Future(Some(searchMock)))

          val response = get(apiEndPoint, extraParams, Map("Host" -> "api.soundcloud.com"))
          response.statusCode ==== 200
          Json.parse(response.contentString) ==== searchMock.json
          doesNotForward(response)

          verify(searchMapperMock).materialize(anonymousSession, page)
      }
    }

    "response contains a caching header" in new Context {
      endpoints.foreach {
        case (apiEndPoint, dispatcherRequest, handler) =>
          val request = com.twitter.finagle.http.Request(apiEndPoint, queryParams.toSeq: _*)
          val query = dispatcherRequest(request)
          val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, queryParams, 0, 10)

          when(searchMapperMock.materialize(anonymousSession, page))
            .thenReturn(Future(Some(searchMock)))

          val response = get(apiEndPoint, Map("q" -> "foo"), Map("Host" -> "api.soundcloud.com"))
          response.statusCode ==== 200
          response.headerMap.get("Cache-Control").get must contain("max-age=" + SearchHandler.MaxCacheAge)
          response.headerMap.get("Cache-Control").get must contain("public")
          doesNotForward(response)

          verify(searchMapperMock).materialize(anonymousSession, page)
      }
    }

    "Pagination error handling" >> {
      "200 when no pagination params" in new Context {
        endpoints.foreach {
          case (apiEndPoint, dispatcherRequest, handler) =>
            val request = com.twitter.finagle.http.Request(apiEndPoint, queryParams.toSeq: _*)
            val query = dispatcherRequest(request)
            val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, queryParams, 0, 10)

            when(searchMapperMock.materialize(anonymousSession, page))
              .thenReturn(Future(Some(searchMock)))

            val response = get(apiEndPoint, Map("q" -> "foo"), Map("Host" -> "api.soundcloud.com"))
            response.statusCode ==== 200
            doesNotForward(response)

            verify(searchMapperMock).materialize(anonymousSession, page)
        }
      }

      "400 when offset/limit is junk" in new Context {
        for {
          (apiEndPoint, dispatcherRequest, handler) <- endpoints
          param <- Seq("offset", "limit")
        } {
          val response =
            get(apiEndPoint, Map("q" -> "foo", param -> "not_a_number"), Map("Host" -> "api.soundcloud.com"))
          response.statusCode ==== 400
          doesNotForward(response)
        }
      }

      "400 when limit/offset is present, but empty" in new Context {
        for {
          (apiEndPoint, dispatcherRequest, handler) <- endpoints
          param <- Seq("offset", "limit")
        } {
          val response =
            get(apiEndPoint, Map("q" -> "foo", "offset" -> ""), Map("Host" -> "api.soundcloud.com"))
          response.statusCode ==== 400
          doesNotForward(response)
        }
      }

      "400 when dispatcher returns a 400" in new Context {
        endpoints.foreach {
          case (apiEndPoint, dispatcherRequest, handler) =>
            val request = com.twitter.finagle.http.Request(apiEndPoint, extraParams.toSeq: _*)
            val query = dispatcherRequest(request)
            val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, extraParams, 0, 10)

            when(searchMapperMock.materialize(anonymousSession, page))
              .thenReturn(Future.exception(RepositoryException(Status.BadRequest, "oh, behave!")))

            val response = get(apiEndPoint, extraParams, Map("Host" -> "api.soundcloud.com"))
            response.statusCode ==== 400
            doesNotForward(response)

            verify(searchMapperMock).materialize(anonymousSession, page)
        }
      }
    }
  }

  "/tracks" >> {
    trait Context extends ForwardContext with TrackRepresentationSpecContext {
      val trackRepresentation = createTrackRepresentation()
      val tracksCollection = Collection(List(trackRepresentation), None)

      val path = "/tracks"
      def paginationParams(path: String) =
        TrackPagination(
          Some(5),
          Some(10),
          true,
          None,
          None,
          new URL("http://api.soundcloud.com" + path)
        )
    }

    "returns track search results" in new Context {
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      searchService.searchTracks(
        anonymousSession,
        Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"),
        paginationParams(path + queryString)
      ) returns Future
        .value(
          tracksCollection
        )
        .outcomeF
      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 200
      response.contentString ==== Collection.getRepresentation(tracksCollection, true)
    }

    "returns a 400 when search service returns invalid request" in new Context {
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      searchService.searchTracks(
        anonymousSession,
        Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"),
        paginationParams(path + queryString)
      ) returns NotValid("not valid").badF

      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 400
      response.contentString ==== Json.stringify(Json.obj("error" -> "invalid request"))
    }
  }

  "/playlists" >> {
    trait Context extends ForwardContext with PlaylistRepresentationSpecContext {
      val playlistsCollections = Collection(List(playlist), None)

      val path = "/playlists"
      searchService.playlistParams returns Seq("linked_partitioning", "q")

      def paginationParams(path: String): OffsetBasedPagination = {
        val mockRequest = Request(path)
        mockRequest.host = "localhost"
        OffsetBasedPagination.build(mockRequest, searchService.playlistParams)
      }
    }

    "returns playlist search results" in new Context {
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      searchService.searchPlaylists(
        anonymousSession,
        Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"),
        paginationParams(path + queryString)
      ) returns Future
        .value(
          playlistsCollections
        )
        .outcomeF
      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 200
      response.contentString ==== Collection.getRepresentation(playlistsCollections, true)
    }

    "returns a 400 when search service returns invalid request" in new Context {
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      searchService.searchPlaylists(
        anonymousSession,
        Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"),
        paginationParams(path + queryString)
      ) returns NotValid("not valid").badF

      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 400
      response.contentString ==== Json.stringify(Json.obj("error" -> "invalid request"))
    }
  }
}
