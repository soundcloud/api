package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
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
import com.soundcloud.apipublic.handler.search.ParamsExtractor._
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}

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

      lazy val queryParams = ParamMap("q" -> "foo")
      lazy val pageParams = ParamMap("offset" -> "0", "limit" -> "10")
      lazy val requestParams: ParamMap = ParamMap(pageParams ++ queryParams)
      lazy val access: AccessParams = AccessParamsExtractor.unapply(queryParams)

      trait UsersParamsHandler extends SearchHandler {
        override protected val userParams: Seq[String] = Seq("linked_partitioning", "q")
      }

      override val handler: SearchHandler = new SearchHandler(
        authentication,
        "http://api.soundcloud.com",
        searchService,
        telemetry
      ) with UsersParamsHandler

      val request = Request("/users", requestParams.toSeq: _*)
      val page = OffsetBasedPagination.build("http://api.soundcloud.com", request, Seq("linked_partitioning", "q"))

      when(searchService.searchUsers(anonymousSession, request.params.asUsersParams, page, access))
        .thenReturn(Good(userCollection).outcomeF)

      val expectedResponse = Collection.getRepresentation(userCollection, false)
    }

    "performs a search when q param is present" in new Context {
      val response = get("/users", requestParams, Map())
      response.statusCode ==== 200
      response.contentString ==== expectedResponse
    }

    "adds wildcard q param to request when not present" in new Context {
      val wildcardParam = Map("q" -> "*")
      override lazy val requestParams = ParamMap(pageParams)

      when(
        searchService
          .searchUsers(anonymousSession, ParamMap(request.params ++ wildcardParam).asUsersParams, page, access)
      ).thenReturn(Good(userCollection).outcomeF)
      val response = get("/users", pageParams, Map())
      response.statusCode ==== 200
      response.contentString ==== expectedResponse
    }

    "response contains a caching header" in new Context {
      val response = get("/users", requestParams, Map())

      response.statusCode ==== 200

      response.headerMap.get("Cache-Control").get must contain("max-age=" + MaxCacheAge)
      response.headerMap.get("Cache-Control").get must contain("public")
    }

    "200 when no pagination params" in new Context {
      override lazy val requestParams = queryParams
      val response = get("/users", request.params, Map())
      response.statusCode ==== 200
    }

    "returns 400 when search service returns error" in new Context {
      when(searchService.searchUsers(anonymousSession, request.params.asUsersParams, page, access))
        .thenReturn(NotValid("not valid").badF)

      val response = get("/users", request.params, Map())
      response.statusCode ==== 400
    }

    "returns 505 when search service returns unknown error" in new Context {
      when(searchService.searchUsers(anonymousSession, request.params.asUsersParams, page, access))
        .thenReturn(HttpServiceError(HttpResponseFields(500)).badF)

      val response = get("/users", request.params, Map())
      response.statusCode ==== 500
    }

  }

  "/tracks" >> {
    trait Context extends ForwardContext with TrackRepresentationSpecContext {
      val trackRepresentation = createTrackRepresentationFromVisibleTrack()
      val tracksCollection = Collection(List(trackRepresentation), None)

      val path = "/tracks"

      lazy val params = ParamMap(("q", "foo"), ("offset", "10"), ("limit", "5"), ("linked_partitioning", "1"))
      lazy val access: AccessParams = AccessParamsExtractor.unapply(params)
      lazy val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      lazy val limit = Some(5)
      lazy val offset = Some(10)

      val page = TrackPagination(
        limit,
        offset,
        true,
        None,
        None,
        new URL("http://api.soundcloud.com" + (path + queryString))
      )

      lazy val response = get("/tracks", params, Map())
    }

    "returns 200 when a valid duration is sent" in new Context {
      override lazy val params = ParamMap(
        "q" -> "foo",
        "offset" -> "10",
        "limit" -> "5",
        "linked_partitioning" -> "1",
        "duration" -> "SHORT"
      )
      lazy val withAccessParams = ParamMap(
        ParamMap(
          "content_tier" -> "FREE",
          "content_country" -> Geo.UNKNOWN_GEO.getCountryCode
        ) ++ params
      )

      when(
        searchService
          .searchTracks(
            ===(anonymousSession),
            ===(withAccessParams.asTracksParams),
            any[TrackPagination],
            ===(access)
          )
      ).thenReturn(Good(tracksCollection).outcomeF)

      response.statusCode ==== 200
    }

    "adds wildcard q param to tracks request when not present" in new Context {
      override lazy val params = ParamMap(
        "q" -> "",
        "offset" -> "10",
        "limit" -> "5",
        "linked_partitioning" -> "1",
        "duration" -> "SHORT"
      )
      lazy val withAccessParams = ParamMap(
        ParamMap(
          "content_tier" -> "FREE",
          "content_country" -> Geo.UNKNOWN_GEO.getCountryCode
        ) ++ params ++ ParamMap("q" -> "*")
      )

      when(
        searchService
          .searchTracks(
            ===(anonymousSession),
            ===(withAccessParams.asTracksParams),
            any[TrackPagination],
            ===(access)
          )
      ).thenReturn(Good(tracksCollection).outcomeF)

      response.statusCode ==== 200
    }

    "does not add wildcard when urns are present but passes q=* to backend" in new Context {
      override lazy val params = ParamMap(
        "urns" -> "soundcloud:tracks:1,soundcloud:tracks:2",
        "offset" -> "10",
        "limit" -> "5",
        "linked_partitioning" -> "1"
      )
      // Backend requires non-empty q; handler adds q=* when only urns/ids present
      lazy val withAccessParams = ParamMap(
        ParamMap(
          "content_tier" -> "FREE",
          "content_country" -> Geo.UNKNOWN_GEO.getCountryCode
        ) ++ params ++ ParamMap("q" -> "*")
      )

      when(
        searchService
          .searchTracks(
            ===(anonymousSession),
            ===(withAccessParams.asTracksParams),
            any[TrackPagination],
            ===(access)
          )
      ).thenReturn(Good(tracksCollection).outcomeF)

      response.statusCode ==== 200
    }

    "adds wildcard q param to tracks request when ids are present" in new Context {
      override lazy val params = ParamMap(
        "ids" -> "1,2,3",
        "offset" -> "10",
        "limit" -> "5",
        "linked_partitioning" -> "1"
      )
      lazy val withAccessParams = ParamMap(
        ParamMap(
          "content_tier" -> "FREE",
          "content_country" -> Geo.UNKNOWN_GEO.getCountryCode
        ) ++ params ++ ParamMap("q" -> "*")
      )

      when(
        searchService
          .searchTracks(
            ===(anonymousSession),
            ===(withAccessParams.asTracksParams),
            any[TrackPagination],
            ===(access)
          )
      ).thenReturn(Good(tracksCollection).outcomeF)
      response.statusCode ==== 200
    }

    "returns 400 when duration filter is not valid" in new Context {
      override lazy val params = ParamMap(
        "q" -> "foo",
        "offset" -> "10",
        "limit" -> "5",
        "linked_partitioning" -> "1",
        "duration" -> "NOT_VALID"
      )
      override lazy val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1&duration=SHORT"
      response.statusCode ==== 400
    }

    "returns track search results" in new Context {
      override lazy val params = ParamMap("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1")
      lazy val withAccessParams = ParamMap(
        params ++ ParamMap(
          "content_tier" -> "FREE",
          "content_country" -> Geo.UNKNOWN_GEO.getCountryCode
        )
      )

      when(searchService.searchTracks(anonymousSession, withAccessParams.asTracksParams, page, access))
        .thenReturn(Good(tracksCollection).outcomeF)

      response.statusCode ==== 200
      response.contentString ==== Collection.getRepresentation(tracksCollection, true)
    }

    "handles duplicate q params without failing" in new Context {
      // Simulate a request like /tracks?q=&q=foo which used to cause a 500 due to a ClassCastException
      when(
        searchService
          .searchTracks(
            any,
            any,
            any[TrackPagination],
            any[AccessParams]
          )
      ).thenReturn(Good(tracksCollection).outcomeF)

      val res = get("/tracks?q=&q=foo")
      res.statusCode ==== 200
    }

    "returns a 400 when search service returns invalid request" in new Context {
      override lazy val params = ParamMap(("q", "foo"), ("offset", "10"), ("limit", "5"), ("linked_partitioning", "1"))
      override lazy val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      lazy val withAccessParams = ParamMap(
        params ++ ParamMap(
          "content_tier" -> "FREE",
          "content_country" -> Geo.UNKNOWN_GEO.getCountryCode
        )
      )
      searchService.searchTracks(
        anonymousSession,
        withAccessParams.asTracksParams,
        page,
        access
      ) returns NotValid("not valid").badF

      response.statusCode ==== 400
    }

    "records missing items" in new Context {
      lazy val withAccessParams = ParamMap(
        params ++ ParamMap(
          "content_tier" -> "FREE",
          "content_country" -> Geo.UNKNOWN_GEO.getCountryCode
        )
      )
      val incompleteTracksCollection = Collection(List(trackRepresentation), Some("next_href"))
      searchService.searchTracks(
        anonymousSession,
        withAccessParams.asTracksParams,
        page,
        access
      ) returns Future.value(incompleteTracksCollection).outcomeF

      response.statusCode ==== 200

      telemetry.getSampleValue("incomplete_paginated_results_total", Seq.empty, Seq.empty) === Some(1)
      telemetry.getSampleValue("missing_paginated_items_total_bucket", Seq("le"), Seq("5.0")) === Some(1.0)
    }
  }

  "/playlists" >> {
    trait Context extends ForwardContext {
      val playlist = new PlaylistBuilder().build
      val playlistsCollections = Collection(List(playlist), None)

      val path = "/playlists"

      trait PlaylistParamsHandler extends SearchHandler {
        override protected val playlistParams: Seq[String] = Seq("linked_partitioning", "q")
      }

      override val handler: SearchHandler = new SearchHandler(
        authentication,
        "http://api.soundcloud.com",
        searchService,
        telemetry
      ) with PlaylistParamsHandler

      def paginationParams(path: String): OffsetBasedPagination = {
        val mockRequest = Request(path)
        OffsetBasedPagination.build("http://api.soundcloud.com", mockRequest, Seq("linked_partitioning", "q"))
      }
    }

    "returns playlist search results" in new Context {
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      val params = ParamMap("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1")
      val access: AccessParams = AccessParamsExtractor.unapply(params)
      searchService.searchPlaylists(
        anonymousSession,
        params.asPlaylistParams,
        paginationParams(path + queryString),
        access
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
      val params = ParamMap("q" -> "*", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1")
      val access: AccessParams = AccessParamsExtractor.unapply(params)
      searchService.searchPlaylists(
        anonymousSession,
        params.asPlaylistParams,
        paginationParams(path + queryString),
        access
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
      val params = ParamMap("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1")
      val queryString = "?q=foo&offset=10&limit=5&linked_partitioning=1"
      val access: AccessParams = AccessParamsExtractor.unapply(params)
      searchService.searchPlaylists(
        anonymousSession,
        params.asPlaylistParams,
        paginationParams(path + queryString),
        access
      ) returns NotValid("not valid").badF

      val response = get(path, Map("q" -> "foo", "offset" -> "10", "limit" -> "5", "linked_partitioning" -> "1"))

      response.statusCode ==== 400
    }
  }
}
