package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.search.SearchDispatcherRequest
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice._
import com.soundcloud.scalakit.test.VerifiedMocks
import play.api.libs.json.JsObject

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
      OffsetBasedPage(query, "http://api-v2.soundcloud.com", "http://api-v2.soundcloud.com/search/tracks", params, 0, 10)

    implicit val context = new MappingContext(mock[UserSession])
  }

  "paginated responses" >> {
    trait PaginatedContext extends Context {
      val request = pagedRequest(Map.empty)
    }

    "maps json to search response" in new PaginatedContext {
      val search = mapper.map(request, json)
      search.json ==== json
    }

    "when there's a next page, updates the offset according to the search response" in new PaginatedContext {
      val search = mapper.map(request, json)
      // tracks.json: 3 results, 5 total results
      search.next_href must beSome { s: String => s.contains("offset=3")}
    }

    "when there's no next page, doesn't include a nextPage" in new PaginatedContext {
      val search = mapper.map(request, emptyJson)
      search.next_href must beNone
    }
  }
}
