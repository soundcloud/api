package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.bff.nextbff.test.JsonMappingMock
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.{MetricsRegistryImpl, Telemetry}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.config.InMemoryConfig
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.search.SearchMapper
import com.soundcloud.publicApiStrangler.mapping.search.{Search, SearchDispatcherRequest}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.Json

class SearchHandlerSpec extends UnitSpecification {

  trait ForwardContext extends HandlerSpecificationScope {
    def followCountsSeq: Seq[FollowCounts] = Seq.empty

    val fallbackMock = mock[DispatchToMothershipHandler]
    val fallbackCounter = new Telemetry((new InMemoryConfig).getApplicationName, MetricsRegistryImpl.defaultRegistry).counter("foo", "bar", "path")
    val searchMapperMock = mock[SearchMapper]
    val followCountsClientMock = mock[FollowCountsClient]
    val lieblingClientMock = mock[LieblingClient]
    val repostsClientMock = mock[RepostsClient]

    val authentication = new FakeUserAuthentication(anonymousSession)
    val userRelatedMothershipDispatcher = new UserRelatedMothershipDispatcher(
      authentication,
      fallbackMock,
      followCountsClientMock,
      lieblingClientMock,
      () => Future.value(true),
      repostsClientMock)

    val trackMothershipDispatcherWithCounts = mock[TrackMothershipDispatcherWithCounts]

    val handler = new SearchHandler(
      authentication,
      fallbackMock,
      fallbackCounter,
      followCountsClientMock,
      searchMapperMock,
      "http://api.soundcloud.com",
      lieblingClientMock,
      userRelatedMothershipDispatcher,
      trackMothershipDispatcherWithCounts
    )

    override def routingDefinitions = Routing.forSearchHandler(handler)

    // just so we can distinguish a forwarded request. Typically, this would be 200.
    val forwardStatus = Status.Found
    val forwardContent = "forwardContent"

    def expectForwardedRequest = {
      val response = JsonResponseBuilder().body(forwardContent).status(forwardStatus).build
      fallbackMock.dispatchToMothership(any[HandlerRequest]) returns Future.value(response)

      fallbackMock.dispatch(any[HandlerRequest])
        .returns(Future(response))

      followCountsClientMock.counts(any[UserSession], any[Seq[Urn]]) returns Future.value(followCountsSeq)
    }

    def stillForwards(response: Response) = {
      response.status ==== forwardStatus
      response.contentString ==== "forwardContent"
    }

    def doesNotForward(response: Response) = {
      there was noCallsTo(fallbackMock)
    }
  }

  "when resource that supports search is called" >> {


    trait Context extends ForwardContext {
      val endpoints = Seq(
        ("/tracks", SearchDispatcherRequest.trackSearch, handler.dispatchTrackRequest),
        ("/tracks/", SearchDispatcherRequest.trackSearch, handler.dispatchTrackRequest),
        ("/tracks.json", SearchDispatcherRequest.trackSearch, handler.dispatchTrackRequest),
        ("/tracks.json/", SearchDispatcherRequest.trackSearch, handler.dispatchTrackRequest),
        ("/v1/tracks", SearchDispatcherRequest.trackSearch, handler.dispatchTrackRequest),
        ("/v1/tracks.json", SearchDispatcherRequest.trackSearch, handler.dispatchTrackRequest),

        ("/users", SearchDispatcherRequest.userSearch, handler.dispatchUserRequest),
        ("/users.json", SearchDispatcherRequest.userSearch, handler.dispatchUserRequest),

        ("/playlists", SearchDispatcherRequest.playlistSearch, handler.dispatchPlaylistRequest),
        ("/playlists.json", SearchDispatcherRequest.playlistSearch, handler.dispatchPlaylistRequest)
      )

      val queryParams = Map("q" -> "foo")
      val pageParams = Map("offset" -> "0", "limit" -> "10")
      val extraParams = pageParams ++ queryParams
      val headers = Map.empty[String, String]

      abstract class SearchMock extends JsonMappingMock with Search

      val searchMock = JsonMappingMock.prepare[SearchMock]
    }

    // TODO fix test
    //    "forwards to Mothership when q param not present" in new Context {
    //      endpoints.foreach { case (apiEndPoint, dispatcherEndPoint) =>
    //        expectForwardedRequest
    //        val response = get(handler, apiEndPoint)
    //        fallbackCounter.labels(apiEndPoint).get() ==== 1.0
    //        stillForwards(response)
    //      }
    //    }

    "performs a search when q param is present" in new Context {
      endpoints.foreach { case (apiEndPoint, dispatcherRequest, handler) =>
        val request = com.twitter.finagle.http.Request(apiEndPoint, extraParams.toSeq: _*)
        val query = dispatcherRequest(request)
        val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, extraParams, 0, 10)

        when(searchMapperMock.materialize(anonymousSession, page))
          .thenReturn(Future(Some(searchMock)))

        val response = get(handler, apiEndPoint, extraParams, Map("Host" -> "api.soundcloud.com"))
        response.statusCode ==== 200
        Json.parse(response.contentString) ==== searchMock.json
        doesNotForward(response)

        verify(searchMapperMock).materialize(anonymousSession, page)
      }
    }

    "response contains a caching header" in new Context {
      endpoints.foreach { case (apiEndPoint, dispatcherRequest, handler) =>
        val request = com.twitter.finagle.http.Request(apiEndPoint, queryParams.toSeq: _*)
        val query = dispatcherRequest(request)
        val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, queryParams, 0, 10)

        when(searchMapperMock.materialize(anonymousSession, page))
          .thenReturn(Future(Some(searchMock)))

        val response = get(handler, apiEndPoint, Map("q" -> "foo"), Map("Host" -> "api.soundcloud.com"))
        response.statusCode ==== 200
        response.headerMap.get("Cache-Control").get must contain("max-age=" + SearchHandler.MaxCacheAge)
        response.headerMap.get("Cache-Control").get must contain("public")
        doesNotForward(response)

        verify(searchMapperMock).materialize(anonymousSession, page)
      }
    }

    "Pagination error handling" >> {
      "200 when no pagination params" in new Context {

        endpoints.foreach { case (apiEndPoint, dispatcherRequest, handler) =>
          val request = com.twitter.finagle.http.Request(apiEndPoint, queryParams.toSeq: _*)
          val query = dispatcherRequest(request)
          val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, queryParams, 0, 10)

          when(searchMapperMock.materialize(anonymousSession, page))
            .thenReturn(Future(Some(searchMock)))

          val response = get(handler, apiEndPoint, Map("q" -> "foo"), Map("Host" -> "api.soundcloud.com"))
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
          val response = get(handler, apiEndPoint, Map("q" -> "foo", param -> "not_a_number"), Map("Host" -> "api.soundcloud.com"))
          response.statusCode ==== 400
          doesNotForward(response)
        }
      }

      "400 when limit/offset is present, but empty" in new Context {
        for {
          (apiEndPoint, dispatcherRequest, handler) <- endpoints
          param <- Seq("offset", "limit")
        } {
          val response = get(handler, apiEndPoint, Map("q" -> "foo", "offset" -> ""), Map("Host" -> "api.soundcloud.com"))
          response.statusCode ==== 400
          doesNotForward(response)
        }
      }

      "400 when dispatcher returns a 400" in new Context {
        endpoints.foreach { case (apiEndPoint, dispatcherRequest, handler) =>
          val request = com.twitter.finagle.http.Request(apiEndPoint, extraParams.toSeq: _*)
          val query = dispatcherRequest(request)
          val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, extraParams, 0, 10)

          when(searchMapperMock.materialize(anonymousSession, page))
            .thenReturn(Future.exception(RepositoryException(Status.BadRequest, "oh, behave!")))

          val response = get(handler, apiEndPoint, extraParams, Map("Host" -> "api.soundcloud.com"))
          response.statusCode ==== 400
          doesNotForward(response)

          verify(searchMapperMock).materialize(anonymousSession, page)
        }
      }
    }
  }
}
