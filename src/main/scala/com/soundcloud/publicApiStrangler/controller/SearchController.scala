package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.telemetry.Counter
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.controller.SearchController._
import com.soundcloud.publicApiStrangler.mapper.search.SearchMapper
import com.soundcloud.publicApiStrangler.mapping.search.SearchDispatcherRequest
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.BadRequestStatus
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Future, Return, Try}
/**
  * Redirects search queries on to search-dispatcher and fetches meta data.
  */
class SearchController(userAuthentication: UserAuthentication,
                       mothershipDispatcher: DispatchToMothershipHandler,
                       mothershipCounter: Counter,
                       followCountsClient: FollowCountsClient,
                       searchMapper: SearchMapper,
                       baseUrl: String,
                       lieblingClient: LieblingClient,
                       userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher) extends BffInjectionBasedController {

  get("/tracks")(dispatchTrackRequest)
  get("/tracks/")(dispatchTrackRequest)
  get("/tracks.json")(dispatchTrackRequest)
  get("/tracks.json/")(dispatchTrackRequest) // yes, really.
  get("/v1/tracks")(dispatchTrackRequest)
  get("/v1/tracks.json")(dispatchTrackRequest)

  get("/users")(dispatchUserRequest)
  get("/users/")(dispatchUserRequest) // some clients use this URL pattern
  get("/users.json")(dispatchUserRequest)

  get("/playlists")(dispatchPlaylistRequest)
  get("/playlists.json")(dispatchPlaylistRequest)

  get("/search")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/search.json")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/search/universal")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/search/universal.json")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/search/people")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/search/people.json")(userRelatedMothershipDispatcher.dispatchToMothership _)

  private def dispatchUserRequest = dispatchRequest(
    defaultParams,
    SearchDispatcherRequest.userSearch,
    userRelatedMothershipDispatcher.dispatchToMothership _
  )

  private def dispatchPlaylistRequest = dispatchRequest(
    playlistParams,
    SearchDispatcherRequest.playlistSearch
  )

  private def dispatchTrackRequest = {
    dispatchRequest(
      trackParams,
      SearchDispatcherRequest.trackSearch
    )
  }

  /**
    * Perform a search for tracks. Logic to determine whether this is a search
    * and if we should forward the request to Mothership.
    */
  private def dispatchRequest(searchParams: Set[String],
                              makeRequest: Request => SearchDispatcherRequest,
                              mothershipDispatcherFn: Request => Future[ResponseBuilder] = mothershipDispatcher.dispatch): BffRequestHandler = { request =>
    if (isSearchRequest(request.params, searchParams)) {
      search(request, makeRequest(request))
    } else {
      // XXX: these are requests like /tracks without query params.
      // Mothership allows callers to page through our users/tracks/... in database order.
      // Do we even want this (afaik undocumented) functionality?
      mothershipCounter.labels(request.path).inc()
      mothershipDispatcherFn(request)
    }
  }

  private def isSearchRequest(params: ParamMap, searchParams: Set[String]): Boolean = {
    val paramsWithContent = params.collect { case (k, v) if v != null && v.nonEmpty => k }.toSet
    (searchParams intersect paramsWithContent).nonEmpty
  }

  private def validateParam(request: Request, param: String, pred: Int => Boolean) = {
    request.params.get(param) match {
      case Some(value) => Try(value.toInt).map(pred)
      case _ => Return(true)
    }
  }

  private def search(request: Request,
                     searchRequest: SearchDispatcherRequest): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      val validPagination = for {
        o <- validateParam(request, "offset", _ >= 0)
        l <- validateParam(request, "limit", _ > 0)
      } yield o && l

      validPagination match {
        case Return(true) =>
          val page = PageBuilder(request, baseUrl)(searchRequest)
            .allowExtraParams(searchRequest.paginationParams + SearchMapper.LinkedPartitioning)
            .buildOffsetBased()
          searchMapper.materialize(session, page).map {
            case Some(info) => render.anyJson(info)
            case _ => render.notFound
          } handle {
            case RepositoryException(BadRequestStatus, _) =>
              render.badRequest
          }

        case _ => Future.value(render.badRequest)
      }
    }.map(_.header("Cache-Control", s"public, max-age=$MaxCacheAge, must-revalidate"))
  }
}

object SearchController {
  val MaxCacheAge = 60
  val defaultParams = Set("q")
  val playlistParams = Set("q", "license")
  val trackParams = Set("q", "genres", "tags", "license")
}

