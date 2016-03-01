package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.Future
import com.soundcloud.bff.finagle.{ResponseBuilder, Request}
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.bff.nextbff.test.JsonMappingMock
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.mapper.search.SearchMapper
import com.soundcloud.publicApiStrangler.mapping.search.{Search, SearchDispatcherRequest}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.BadRequestStatus
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.test.VerifiedMocks
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.mockito.Mockito.times

class SearchControllerSpec extends InjectionBasedControllerSpecification {

  trait ForwardContext extends Scope {
    def followCountsSeq: Seq[FollowCounts] = Seq.empty
    val fallbackMock = mock[DispatchToMothershipHandler]
    val searchMapperMock = mock[SearchMapper]
    val followCountsClientMock = mock[FollowCountsClient]

    val controller = new SearchController(
      fakeUserAuthentication(anonymousSession),
      fallbackMock,
      followCountsClientMock,
      _ => Future.value(avoidMothershipFlag),
      searchMapperMock,
      "http://api.soundcloud.com"
    )

    // just so we can distinguish a forwarded request. Typically, this would be 200.
    val forwardStatus = HttpResponseStatus.FOUND.getCode
    val forwardContent = "forwardContent"

    def expectForwardedRequest = {
      val responseBuilder = new ResponseBuilder().body(forwardContent).status(forwardStatus)
      fallbackMock.defaultHandling(any[HandlerRequest]) returns Future.value(responseBuilder.build)

      fallbackMock.dispatch(any[Request])
        .returns(Future(responseBuilder))

      followCountsClientMock.counts(any[UserSession], any[Seq[Urn]]) returns Future.value(followCountsSeq)
    }

    def stillForwards(response: MockResponse) = {
      response.code ==== forwardStatus
      response.body ==== "forwardContent"
    }

    def doesNotForward(response: MockResponse) = {
      there was noCallsTo(fallbackMock)
    }

    def avoidMothershipFlag: Boolean
  }


  "when resource that supports search is called" >> {

    val endpoints = Seq(
      ("/tracks", SearchDispatcherRequest.trackSearch),
      ("/tracks/", SearchDispatcherRequest.trackSearch),
      ("/tracks.json", SearchDispatcherRequest.trackSearch),
      ("/tracks.json/", SearchDispatcherRequest.trackSearch),
      ("/v1/tracks", SearchDispatcherRequest.trackSearch),
      ("/v1/tracks.json", SearchDispatcherRequest.trackSearch),

      ("/users", SearchDispatcherRequest.userSearch),
      ("/users.json", SearchDispatcherRequest.userSearch),

      ("/groups", SearchDispatcherRequest.groupSearch),
      ("/groups.json", SearchDispatcherRequest.groupSearch),

      ("/playlists", SearchDispatcherRequest.playlistSearch),
      ("/playlists.json", SearchDispatcherRequest.playlistSearch)

      //        ("/search", UniversalPath, searchMapperMock),
      //        ("/search/playlists", PlaylistsPath, searchMapperMock),
      //        ("/search/groups", GroupsPath, searchMapperMock)
    )

    trait Context extends VerifiedMocks with ForwardContext {
      val queryParams = Map("q" -> "foo")
      val pageParams = Map("offset" -> "0", "limit" -> "10")
      val extraParams = pageParams ++ queryParams
      val headers = Map.empty[String,String]

      abstract class SearchMock extends JsonMappingMock with Search

      val searchMock = JsonMappingMock.prepare[SearchMock]
    }

    trait EnabledContext extends Context {
      def avoidMothershipFlag = true
    }

    trait DisabledContext extends Context {
      def avoidMothershipFlag = false
    }

    "forwards to Mothership when q param not present" in new Context {
      def avoidMothershipFlag = true

      endpoints.foreach { case (apiEndPoint, dispatcherEndPoint) =>
        expectForwardedRequest
        val response = get(controller, apiEndPoint)
        stillForwards(response)
      }
    }

    "forwards to Mothership when feature not enabled" in new DisabledContext {
      endpoints.foreach { case (apiEndPoint, dispatcherEndPoint) =>
        expectForwardedRequest
        val response = get(controller, apiEndPoint, extraParams, Map("Host" -> "api.soundcloud.com"))
        stillForwards(response)
      }
    }

    "performs a search when q param is present" in new EnabledContext {
      endpoints.foreach { case (apiEndPoint, dispatcherRequest) =>
        val request = com.twitter.finagle.http.Request(apiEndPoint, extraParams.toSeq: _*)
        val query = dispatcherRequest(request)
        val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, extraParams, 0, 10)

        when(verified(searchMapperMock).materialize(anonymousSession, page))
          .thenReturn(Future(Some(searchMock)))

        val response = get(controller, apiEndPoint, extraParams, Map("Host" -> "api.soundcloud.com"))
        response.code ==== 200
        response.jsonBody ==== searchMock.json
        doesNotForward(response)
      }
    }

    "response contains a caching header" in new Context {
      def avoidMothershipFlag = true

      endpoints.foreach { case (apiEndPoint, dispatcherRequest) =>
        val request = com.twitter.finagle.http.Request(apiEndPoint, queryParams.toSeq: _*)
        val query = dispatcherRequest(request)
        val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, queryParams, 0, 10)

        when(verified(searchMapperMock).materialize(anonymousSession, page))
          .thenReturn(Future(Some(searchMock)))

        val response = get(controller, apiEndPoint, Map("q" -> "foo"), Map("Host" -> "api.soundcloud.com"))
        response.code ==== 200
        response.getHeader("Cache-Control") must contain ("max-age=" + SearchController.MaxCacheAge)
        response.getHeader("Cache-Control") must contain ("public")
        doesNotForward(response)
      }
    }

    "Pagination error handling" >> {
      "200 when no pagination params" in new Context {
        def avoidMothershipFlag = true

        endpoints.foreach { case (apiEndPoint, dispatcherRequest) =>
          val request = com.twitter.finagle.http.Request(apiEndPoint, queryParams.toSeq: _*)
          val query = dispatcherRequest(request)
          val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, queryParams, 0, 10)

          when(verified(searchMapperMock).materialize(anonymousSession, page))
            .thenReturn(Future(Some(searchMock)))

          val response = get(controller, apiEndPoint, Map("q" -> "foo"), Map("Host" -> "api.soundcloud.com"))
          response.code ==== 200
          doesNotForward(response)
        }

      }

      "400 when offset/limit is junk" in new Context {
        def avoidMothershipFlag = true

        for {
          (apiEndPoint, dispatcherRequest) <- endpoints
          param <- Seq("offset", "limit")
        } {
          val response = get(controller, apiEndPoint, Map("q" -> "foo", param -> "not_a_number"), Map("Host" -> "api.soundcloud.com"))
          response.code ==== 400
          doesNotForward(response)
        }
      }

      "400 when limit/offset is present, but empty" in new Context {
        def avoidMothershipFlag = true

        for {
          (apiEndPoint, dispatcherRequest) <- endpoints
          param <- Seq("offset", "limit")
        } {
          val response = get(controller, apiEndPoint, Map("q" -> "foo", "offset" -> ""), Map("Host" -> "api.soundcloud.com"))
          response.code ==== 400
          doesNotForward(response)
        }
      }

      "400 when dispatcher returns a 400" in new Context {
        def avoidMothershipFlag = true

        endpoints.foreach { case (apiEndPoint, dispatcherRequest) =>
          val request = com.twitter.finagle.http.Request(apiEndPoint, extraParams.toSeq: _*)
          val query = dispatcherRequest(request)
          val page = OffsetBasedPage(query, "http://api.soundcloud.com", apiEndPoint, extraParams, 0, 10)

          when(verified(searchMapperMock).materialize(anonymousSession, page))
            .thenReturn(Future.exception(RepositoryException(BadRequestStatus, "oh, behave!")))

          val response = get(controller, apiEndPoint, extraParams, Map("Host" -> "api.soundcloud.com"))
          response.code ==== 400
          doesNotForward(response)
        }
      }
    }
  }
}
