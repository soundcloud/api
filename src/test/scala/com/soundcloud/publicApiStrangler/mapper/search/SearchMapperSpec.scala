package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.search.{LegacySearch, PaginatedSearch, SearchDispatcherRequest}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice._
import com.soundcloud.scalakit.test.VerifiedMocks
import play.api.libs.json.{JsArray, JsObject}

class SearchMapperSpec extends UnitSpecification {

  val json = withContentsOf("search", "tracks").as[JsObject]
  val urns = (json \ "docs").as[List[JsObject]].map(doc => Urn((doc \ "urn").as[String]))
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

    implicit val context = new MappingContext(mock[UserSession])
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
            search.next_href must beSome { s: String => s.contains("offset=3")}
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
            search.value.size ==== 3
          case x => failure("bad mapping, expected LegacySearch, got " + x)
        }
      }
    }

    "empty response" in new LegacyContext {
      requests.foreach { request =>
        mapper.map(request, emptyJson) match {
          case search: LegacySearch => search.value.size ==== 0
          case x => failure("bad mapping, expected LegacySearch, got " + x)
        }
      }
    }
  }
}
