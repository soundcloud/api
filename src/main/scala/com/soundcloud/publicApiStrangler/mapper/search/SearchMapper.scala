package com.soundcloud.publicApiStrangler.mapper.search

import java.net.URLDecoder

import com.soundcloud.bff.nextbff.mapper.FetchMapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.nextbff.repository.{IndividualFetchRepository, SafeJsonHandler}
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Param, Params, StringParam, UrnParam, UrnsParam}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.HeadersBuilder
import com.soundcloud.jvmkit.module.util.session.UserSession
import play.api.libs.json.JsValue

class SearchRepository(searchService: JsonClient)
    extends SafeJsonHandler
    with IndividualFetchRepository[OffsetBasedPage[SearchDispatcherRequest]] {
  override def fetch(session: UserSession, input: OffsetBasedPage[SearchDispatcherRequest]) = {
    val decodedParams = createParams(session, input).map {
      case (k, urn @ UrnParam(_)) => (k, urn: Param)
      case (k, urns @ UrnsParam(_)) => (k, urns: Param)
      case (k, param) => (k, param.value.map(URLDecoder.decode(_, "utf-8")).head: Param)
    }
    val headers = input.param.searchHeaders
      .foldLeft(new HeadersBuilder) {
        case (builder, (key, value)) => builder.set(key, value)
      }
      .build

    searchService
      .getWithSession(session, input.param.searchPath, decodedParams, headers)
      .map(toJsonObject)
      .map(Option(_))
  }

  def createParams(session: UserSession, input: OffsetBasedPage[SearchDispatcherRequest]): Params = {
    val dispatcherRequest = input.param
    val mappedInputParams =
      dispatcherRequest.mapParams(input.extraParams.filterKeys(_ != SearchMapper.LinkedPartitioning))
    dispatcherRequest.searchPath match {
      case _ => mappedInputParams
    }
  }
}

object SearchRepository {
  val UsersPath = Path() / "search" / "users"
}

class SearchMapper(val repository: SearchRepository, submapper: SearchEntityMapper, baseUrl: String)
    extends FetchMapper[OffsetBasedPage[SearchDispatcherRequest], Search] {
  private def shouldPaginate(params: Params) = {
    val param = params.getOrElse(SearchMapper.LinkedPartitioning, StringParam(""))
    param.value.exists(_.nonEmpty)
  }

  override def map(dispatcherRequest: OffsetBasedPage[SearchDispatcherRequest], json: JsValue)(
      implicit context: MappingContext
  ): Search =
    if (shouldPaginate(dispatcherRequest.extraParams)) new JsonMapping(json) with PaginatedSearch {
      override protected def currentPage: OffsetBasedPage[_] = dispatcherRequest

      override def entityMapper: SearchEntityMapper = submapper
    }
    else
      new JsonMapping(json) with LegacySearch {
        override def entityMapper: SearchEntityMapper = submapper
      }
}

object SearchMapper {
  val LinkedPartitioning = "linked_partitioning"
}
