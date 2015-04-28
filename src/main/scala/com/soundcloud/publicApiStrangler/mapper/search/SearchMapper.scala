package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapper.FetchMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.nextbff.repository.{IndividualFetchRepository, SafeJsonHandler}
import com.soundcloud.bff.repository.JsonServiceRepository
import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.search.{Search, LegacySearch, PaginatedSearch, SearchDispatcherRequest}
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.finagle.jsonservice.{Params, StringParam}
import play.api.libs.json.JsValue


class SearchRepository(searchService: JsonService)
  extends JsonServiceRepository(searchService) with SafeJsonHandler with IndividualFetchRepository[OffsetBasedPage[SearchDispatcherRequest]] {

  override def fetch(session: UserSession, input: OffsetBasedPage[SearchDispatcherRequest]) =
    fetch(session,
      input.param.searchPath,
      input.param.mapParams(input.extraParams.filterKeys(_ != SearchMapper.LinkedPartitioning)),
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

  private def shouldPaginate(params: Params) = {
    val param = params.getOrElse(SearchMapper.LinkedPartitioning, StringParam(""))
    param.value.exists(_.nonEmpty)
  }

  override def map(dispatcherRequest: OffsetBasedPage[SearchDispatcherRequest], json: JsValue)
                  (implicit context: MappingContext): Search =
    if (shouldPaginate(dispatcherRequest.extraParams)) new JsonMapping(json) with PaginatedSearch {

      override protected def currentPage: OffsetBasedPage[_] = dispatcherRequest

      override def entityMapper: SearchEntityMapper = submapper
    }
    else new JsonMapping(json) with LegacySearch {
      override def entityMapper: SearchEntityMapper = submapper
    }
}

object SearchMapper {
  val LinkedPartitioning = "linked_partitioning"
}
