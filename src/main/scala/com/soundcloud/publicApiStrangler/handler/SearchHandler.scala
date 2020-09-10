package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.bff.nextbff.UntypedJson
import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.handler.SearchHandler._
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.mapper.search.{SearchDispatcherRequest, SearchMapper}
import com.soundcloud.publicApiStrangler.service.SearchService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackPagination
import com.twitter.finagle.http.{ParamMap, Response, Status}
import com.twitter.util.{Future, Return, Try}

/**
  * Redirects search queries on to search-dispatcher and fetches meta data.
  */
class SearchHandler(
    userAuthentication: UserAuthentication,
    searchMapper: SearchMapper,
    baseUrl: String,
    userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher,
    searchService: SearchService
) {

  def dispatchUserRequest = dispatchRequest(
    defaultParams,
    SearchDispatcherRequest.userSearch,
    userRelatedMothershipDispatcher.dispatchToMothership _
  )

  def searchTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val hasLinkedPartitioning = req.params.contains("linked_partitioning")
      val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

      val tracksCollection = searchService
        .searchTracks(session, req.params, pagination)
        .value
      CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning)
    }
  }

  def searchPlaylists(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val hasLinkedPartitioning = req.params.contains("linked_partitioning")
      val pagination = OffsetBasedPagination.build(req, Seq("linked_partitioning") ++ searchService.playlistParams)

      val playlistsCollection =
        searchService
          .searchPlaylists(session, req.params, pagination)
          .value
      CollectionResponse.handleCollectionResponse(playlistsCollection, hasLinkedPartitioning)
    }
  }

  /**
    * Perform a search for a given entity. Logic to determine whether this is a search
    * and if we should forward the request to Mothership.
    */
  private def dispatchRequest(
      searchParams: Set[String],
      makeRequest: HandlerRequest => SearchDispatcherRequest,
      mothershipDispatcherFn: Handler
  ): Handler = { request =>
    userAuthentication
      .withUserSession(request) { session =>
        if (isSearchRequest(request.params, searchParams)) {
          search(request, session, makeRequest(request))
        } else {
          mothershipDispatcherFn(request)
        }
      }
  }

  private def isSearchRequest(params: ParamMap, searchParams: Set[String]): Boolean = {
    val paramsWithContent = params.collect { case (k, v) if v != null && v.nonEmpty => k }.toSet
    (searchParams intersect paramsWithContent).nonEmpty
  }

  private def validateParam(request: HandlerRequest, param: String, pred: Int => Boolean) = {
    request.params.get(param) match {
      case Some(value) => Try(value.toInt).map(pred)
      case _ => Return(true)
    }
  }

  private def search(
      request: HandlerRequest,
      session: UserSession,
      searchRequest: SearchDispatcherRequest
  ): Future[Response] = {
    val validPagination = for {
      o <- validateParam(request, "offset", _ >= 0)
      l <- validateParam(request, "limit", _ > 0)
    } yield o && l

    val response = validPagination match {
      case Return(true) =>
        val page = PageBuilder(request, baseUrl)(searchRequest)
          .allowExtraParams(searchRequest.paginationParams + SearchMapper.LinkedPartitioning)
          .buildOffsetBased()
        searchMapper.materialize(session, page).map {
          case Some(info) => JsonResponseBuilder.ok(UntypedJson.write(info))
          case _ => ResponseBuilder.notFound()
        } handle {
          case RepositoryException(Status.BadRequest, _) =>
            ResponseBuilder.badRequest()
        }

      case _ => Future.value(ResponseBuilder.badRequest())
    }
    response.map(appendCacheHeaders(_))
    response
  }

  private def appendCacheHeaders(response: Response) = {
    response.headerMap.set("Cache-Control", s"public, max-age=$MaxCacheAge, must-revalidate")
    response
  }
}

object SearchHandler {
  val MaxCacheAge = 60
  val defaultParams = Set("q")
  val playlistParams = Set("q", "license")
  val trackParams = Set("q", "genres", "tags", "license")
  val allowedFilters = List(
    "q",
    "tags",
    "filter",
    "license",
    "bpm[from]",
    "bpm[to]",
    "duration[from]",
    "duration[to]",
    "created_at[from]",
    "created_at[to]",
    "ids",
    "genres",
    "types"
  )
}
