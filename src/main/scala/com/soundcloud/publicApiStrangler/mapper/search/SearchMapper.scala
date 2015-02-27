package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapper.FetchMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.nextbff.repository.{IndividualFetchRepository, SafeJsonHandler}
import com.soundcloud.bff.repository.JsonServiceRepository
import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.search.{Search, SearchDispatcherRequest}
import com.soundcloud.scalakit._
import play.api.libs.json.JsValue


class SearchRepository(searchService: JsonService)
  extends JsonServiceRepository(searchService) with SafeJsonHandler with IndividualFetchRepository[OffsetBasedPage[SearchDispatcherRequest]] {

  override def fetch(session: UserSession, input: OffsetBasedPage[SearchDispatcherRequest]) =
    fetch(session,
      input.param.searchPath,
      input.param.mapParams(input.extraParams),
      input.param.searchHeaders)
      .map(toJsonObject).map(Option(_))
}

object SearchRepository {
  val UniversalPath = Path() / "search" / "universal"
  val TracksPath = Path() / "search" / "tracks"
  val UsersPath = Path() / "search" / "users"
  val PlaylistsPath = Path() / "search" / "playlists"
  val GroupsPath = Path() / "search" / "groups"
}


class SearchMapper(val repository: SearchRepository,
                   submapper: SearchEntityMapper,
                   baseUrl: String)
  extends FetchMapper[OffsetBasedPage[SearchDispatcherRequest], JsonMapping] {

  override def map(dispatcherRequest: OffsetBasedPage[SearchDispatcherRequest], json: JsValue)
                  (implicit context: MappingContext): Search = new JsonMapping(json) with Search {

      override protected def currentPage: OffsetBasedPage[_] = dispatcherRequest

      override def entityMapper: SearchEntityMapper = submapper
    }
}
