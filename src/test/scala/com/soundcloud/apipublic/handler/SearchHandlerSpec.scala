package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.reposts.RepostsClient
import com.soundcloud.apipublic.handler.representation.collection.CollectionResponse.MaxCacheAge
import com.soundcloud.apipublic.handler.search.SearchHandler
import com.soundcloud.apipublic.service.SearchService
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.PlaylistBuilder
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{TrackPagination, TrackRepresentationSpecContext}
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{ParamMap, Request}
import com.twitter.util.Future
import org.mockito.Mockito.when

import java.net.URL

class SearchHandlerSpec extends UnitSpecification {

  trait ForwardContext extends HandlerSpecificationScope {

    def followCountsSeq: Seq[FollowCounts] = Seq.empty

    val followCountsClientMock = mock[FollowCountsClient]
    val repostsClientMock = mock[RepostsClient]
    val exceptionCollector = new ExceptionCollector(Telemetry.createIsolatedInstance)
    val searchService = mock[SearchService]
    val telemetry = Telemetry.createIsolatedInstance
    val authentication = new FakeUserAuthentication(anonymousSession)

    val handler = new SearchHandler(
      authentication,
      "http://api.soundcloud.com",
      searchService,
      telemetry
    )

    override def routingDefinitions = Routing.forSearchHandler(handler)
  }

  "/users" >> {
    trait Context extends ForwardContext {
      val userUrn = Urn("soundcloud", "users", "88")
      val user = new UserBuilder().setUrn(userUrn).build
      val userCollection = Collection[UserRepresentation](items = List(user), nextHref = None)

      lazy val queryParams = Map("q" -> "foo")
      lazy val pageParams = Map("offset" -> "0", "limit" -> "10")
      lazy val requestParams = pageParams ++ queryParams
      searchService.userParams returns Seq("linked_partitioning", "q")

      val request = Request("/users", requestParams.toSeq: _*)
      request.host = "localhost"
      val page = OffsetBasedPagination.build(request, searchService.userParams)

      when(searchService.searchUsers(anonymousSession, request.params, page))
        .thenReturn(Good(userCollection).outcomeF)

      val expectedResponse = Collection.getRepresentation(userCollection, false)
    }

    "performs a search when q param is present" in new Context {
      val response = get("/users", requestParams, Map("Host" -> "localhost"))
      response.statusCode ==== 200
      response.contentString ==== expectedResponse
    }

    "adds wildcard q param to request when not present" in new Context {
      val wildcardParam = Map("q" -> "*")
      override lazy val requestParams = pageParams

      when(searchService.searchUsers(anonymousSession, request.params ++ wildcardParam, page))
        .thenReturn(Good(userCollection).outcomeF)
      val response = get("/users", pageParams, Map("Host" -> "localhost"))
      response.statusCode ==== 200
      response.contentString ==== expectedResponse
    }

    "response contains a caching header" in new Context {
      val response = get("/users", requestParams, Map("Host" -> "localhost"))

      response.statusCode ==== 200

      response.headerMap.get("Cache-Control").get must contain("max-age=" + MaxCacheAge)
      response.headerMap.get("Cache-Control").get must contain("public")
    }

    "200 when no pagination params" in new Context {
      override lazy val requestParams = queryParams
      val response = get("/users", request.params, Map("Host" -> "localhost"))
      response.statusCode ==== 200
    }

    "returns 400 when search service returns error" in new Context {
      when(searchService.searchUsers(anonymousSession, request.params, page))
        .thenReturn(NotValid("not valid").badF)

      val response = get("/users", request.params, Map("Host" -> "localhost"))
      response.statusCode ==== 400
    }

    "returns 505 when search service returns unknown error" in new Context {
      when(searchService.searchUsers(anonymousSession, request.params, page))
        .thenReturn(HttpServiceError(HttpResponseFields(500)).badF)

      val response = get("/users", request.params, Map("Host" -> "localhost"))
      response.statusCode ==== 500
    }

  }

  "/tracks" >> {
    trait Context extends ForwardContext with TrackRepresentationSpecContext {
      val trackRepresentation = createTrackRepresentationFromVisibleTrack()
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
        ParamMap(("q", "foo"), ("offset", "10"), ("limit", "5"), ("linked_partitioning", "1")),
        paginationParams(path + queryString)
      ) returns Future.value(tracksCollection).outcomeF

      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 200
      response.contentString ==== Collection.getRepresentation(tracksCollection, true)
    }

    "returns a 400 when search service returns invalid request" in new Context {
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      searchService.searchTracks(
        anonymousSession,
        ParamMap(("q", "foo"), ("offset", "10"), ("limit", "5"), ("linked_partitioning", "1")),
        paginationParams(path + queryString)
      ) returns NotValid("not valid").badF

      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 400
    }

    "records missing items" in new Context {
      val incompleteTracksCollection = Collection(List(trackRepresentation), Some("next_href"))
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      val params = ParamMap(("q", "foo"), ("offset", "10"), ("limit", "5"), ("linked_partitioning", "1"))
      searchService.searchTracks(
        anonymousSession,
        params,
        paginationParams(path + queryString)
      ) returns Future.value(incompleteTracksCollection).outcomeF

      get(path, params)

      telemetry.getSampleValue("incomplete_paginated_results_total", Seq.empty, Seq.empty) === Some(1)
      telemetry.getSampleValue("missing_paginated_items_total_bucket", Seq("le"), Seq("5.0")) === Some(1.0)
    }
  }

  "/playlists" >> {
    trait Context extends ForwardContext {
      val playlist = new PlaylistBuilder().build
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
        ParamMap("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"),
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

    "adds wildcard q param to request when not present" in new Context {
      val queryString = "?offset=10&limit=5&linked_partitioning=1"
      searchService.searchPlaylists(
        anonymousSession,
        ParamMap("q" -> "*", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"),
        paginationParams(path + queryString)
      ) returns Future
        .value(
          playlistsCollections
        )
        .outcomeF
      val response = get(path, Map("offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 200
      response.contentString ==== Collection.getRepresentation(playlistsCollections, true)
    }

    "returns a 400 when search service returns invalid request" in new Context {
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      searchService.searchPlaylists(
        anonymousSession,
        ParamMap("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"),
        paginationParams(path + queryString)
      ) returns NotValid("not valid").badF

      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 400
    }
  }
}
