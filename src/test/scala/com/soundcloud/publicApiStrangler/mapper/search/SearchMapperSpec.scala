package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.services.{JsonService => BffJsonService}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationRules
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.mapping.search.{LegacySearch, PaginatedSearch, SearchDispatcherRequest}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.Urn.format
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice._
import com.soundcloud.scalakit.json.{Json => ScalakitJson}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.service.client.OkidokiClient
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsArray, JsNull, JsObject, Json => PlayJson}

class SearchRepositorySpec extends UnitSpecification {
  trait Context extends VerifiedMocks {
    val highTierParams = Params("filter.content_tier" -> "FREE", "filter.content_country" -> "US")

    lazy val session = loggedInSession(Urn("soundcloud:users:123"))
    lazy val mockService = mock[BffJsonService]
    lazy val repo = new SearchRepository(mockService)
  }

  "track search" >> {
    "adds filter.content_type=FREE and filter.content_country=<countryCode>" in new Context {
      val response = withContentsOf("search", "tracks").as[JsObject]
      doReturn(Future.value(JsonResponse(OkStatus, response))).when(mockService)
        .get(session, SearchRepository.TracksPath, highTierParams + ("q" -> "bar"), Params.empty)
      val request = OffsetBasedPage(
        SearchDispatcherRequest(SearchRepository.TracksPath, Set.empty, Map.empty)(x => x),
        "http://localhost", "/search/tracks", Params("q" -> "bar"), 0, 10
      )
      Await.result(repo.fetch(session, request)) ==== Some(response)
    }
  }

  "universal search" >> {
    "adds filter.content_type=FREE and filter.content_country=<countryCode>" in new Context {
      val response = withContentsOf("search", "tracks").as[JsObject]
      doReturn(Future.value(JsonResponse(OkStatus, response))).when(mockService)
        .get(session, SearchRepository.UniversalPath, highTierParams + ("q" -> "bar"), Params.empty)
      val request = OffsetBasedPage(
        SearchDispatcherRequest(SearchRepository.UniversalPath, Set.empty, Map.empty)(x => x),
        "http://localhost", "/search/universal", Params("q" -> "bar"), 0, 10
      )
      Await.result(repo.fetch(session, request)) ==== Some(response)
    }
  }

}

class SearchMapperSpec extends UnitSpecification {

  val json = withContentsOf("search", "tracks").as[JsObject]
  val urns = (json \ "docs").as[List[JsObject]]
    .map(doc => (doc \ "urn").as[Urn])
  val emptyJson = withContentsOf("search", "empty_result").as[JsObject]

  trait Context extends Scope with VerifiedMocks {
    val entityMapperMock = mock[SearchEntityMapper]
    val searchRepoMock = mock[SearchRepository]

    val baseUrl = "http://example.org"
    val mapper = new SearchMapper(searchRepoMock, entityMapperMock, baseUrl)
    val path = SearchRepository.TracksPath
    val query = SearchDispatcherRequest(path, Set.empty, Map.empty)(identity)

    def pagedRequest(params: Map[String, Param]) =
      OffsetBasedPage(query, "http://api-v2.soundcloud.com", "http://api-v2.soundcloud.com/search/tracks", params, 0, 3)

    val sessionMock = mock[UserSession]
    implicit val context = new MappingContext(sessionMock)
  }

  "filters search results that aren't known to okidoki" in new Context {
    val okidokiMock = mock[OkidokiClient]
    val caMock = mock[ContentAuthorizationRules]
    val followCountsClient = mock[FollowCountsClient]
    val searchEntityMapper = new SearchEntityMapper(okidokiMock, followCountsClient, baseUrl, caMock, null, null, null, null)
    val searchMapper = new SearchMapper(searchRepoMock, searchEntityMapper, baseUrl)
    private val request = pagedRequest(Map.empty)

    // search returns a bunch of tracks
    when(searchRepoMock.bulkFetch(sessionMock, Set(request)))
      .thenReturn(Future.value(Map(request -> json)))

    // okidoki doesn't know about it and returns an empty result
    when(okidokiMock.fetch(sessionMock, urns.toSet))
      .thenReturn(Future.value(List.empty))

    when(caMock.fetchRules(===(sessionMock), any[Seq[Urn]]))
      .thenReturn(Future.value(Seq.empty))

    when(followCountsClient.counts(sessionMock, Seq.empty))
      .thenReturn(Future.value(Seq.empty))


    val mapped = Await.result(searchMapper.materialize(sessionMock, request)).get
    val result = PlayJson.parse(ScalakitJson.asString(mapped)).as[JsArray]

    result.value.count { _ == JsNull } ==== 0
  }

  "paginated responses" >> {
    trait PaginatedContext extends Context {
      val requests = Seq("true", "false", "anything").map { v =>
        pagedRequest(Map(SearchMapper.LinkedPartitioning -> v))
      }
    }

    "maps json to search response" in new PaginatedContext {
      requests.foreach { request =>
        mapper.map(request, json) match {
          case search: PaginatedSearch => search.json ==== json
          case x => failure("bad mapping, expected PaginatedSearch, got " + x)
        }
      }
    }

    "when there's a next page, updates the offset according to the search response" in new PaginatedContext {
      requests.foreach { request =>
        mapper.map(request, json) match {
          case search: PaginatedSearch =>
            // tracks.json: 3 results, 5 total results
            search.next_href must beSome { s: String => s.contains("offset=3") }
          case x => failure("bad mapping, expected PaginatedSearch, got " + x)
        }
      }
    }

    "when there's no next page, doesn't include a nextPage" in new PaginatedContext {
      requests.foreach { request =>
        mapper.map(request, emptyJson) match {
          case search: PaginatedSearch => search.next_href must beNone
          case x => failure("bad mapping, expected PaginatedSearch, got " + x)
        }
      }
    }
  }

  "legacy response" >> {
    trait LegacyContext extends Context {
      val requests = Seq(
        pagedRequest(Map(SearchMapper.LinkedPartitioning -> "")),
        pagedRequest(Params.empty)
      )
    }

    "maps json to search response" in new LegacyContext {
      requests.foreach { request =>
        mapper.map(request, json) match {
          case search: LegacySearch =>
            val docs = (json \ "docs").as[JsArray]
            search.json ==== json
          case x => failure("bad mapping, expected LegacySearch, got " + x)
        }
      }
    }

    "empty response" in new LegacyContext {
      requests.foreach { request =>
        mapper.map(request, emptyJson) match {
          case search: LegacySearch => search.json ==== emptyJson
          case x => failure("bad mapping, expected LegacySearch, got " + x)
        }
      }
    }
  }
}
