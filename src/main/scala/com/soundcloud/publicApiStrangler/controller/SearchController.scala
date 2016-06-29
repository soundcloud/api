package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.SearchControllerRolloutChecks
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
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
class SearchController(val userAuthentication: UserAuthentication,
                       val mothershipDispatcher: DispatchToMothershipHandler,
                       val followCountsClient: FollowCountsClient,
                       avoidMothershipFor: SearchControllerRolloutChecks,
                       searchMapper: SearchMapper,
                       baseUrl: String)
  extends BffInjectionBasedController with FollowCountsHelper {
  
  get("/tracks")(dispatchTrackRequest)
  get("/tracks/")(dispatchTrackRequest)
  get("/tracks.json")(dispatchTrackRequest)
  get("/tracks.json/")(dispatchTrackRequest) // yes, really.
  get("/v1/tracks")(dispatchTrackRequest)
  get("/v1/tracks.json")(dispatchTrackRequest)

  get("/users")(dispatchUserRequest)
  get("/users.json")(dispatchUserRequest)

  get("/groups")(dispatchGroupRequest)
  get("/groups.json")(dispatchGroupRequest)

  get("/playlists")(dispatchPlaylistRequest)
  get("/playlists.json")(dispatchPlaylistRequest)

  // NOTE: The following are a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  // These endpoints are NOT properly strangled.
  get("/search")(dispatchToMothershipWithFollowCounts)
  get("/search.json")(dispatchToMothershipWithFollowCounts)

  get("/search/universal")(dispatchToMothershipWithFollowCounts)
  get("/search/universal.json")(dispatchToMothershipWithFollowCounts)

  get("/search/people")(dispatchToMothershipWithFollowCounts)
  get("/search/people.json")(dispatchToMothershipWithFollowCounts)

  private def dispatchUserRequest = dispatchRequest(
    defaultParams,
    SearchDispatcherRequest.userSearch,
    () => avoidMothershipFor.avoidForUsers,
    dispatchToMothershipWithFollowCounts _
  )

  private def dispatchGroupRequest = dispatchRequest(
    defaultParams,
    SearchDispatcherRequest.groupSearch,
    () => avoidMothershipFor.avoidForGroups
  )

  private def dispatchPlaylistRequest = dispatchRequest(
    playlistParams,
    SearchDispatcherRequest.playlistSearch,
    () => avoidMothershipFor.avoidForPlaylists
  )

  private def dispatchTrackRequest = {
    dispatchRequest(
      trackParams,
      SearchDispatcherRequest.trackSearch,
      () => avoidMothershipFor.avoidForTracks
    )
  }

  /**
   * Perform a search for tracks. Logic to determine whether this is a search
   * and if we should forward the request to Mothership.
   */
  private def dispatchRequest(searchParams: Set[String],
                              makeRequest: Request => SearchDispatcherRequest,
                              avoidMothershipFor: () => Future[Boolean],
                              mothershipDispatcherFn: Request => Future[ResponseBuilder] = mothershipDispatcher.dispatch): BffRequestHandler = { request =>
    if (isSearchRequest(request.params, searchParams)) {
      search(request, makeRequest(request), avoidMothershipFor, mothershipDispatcherFn)
    } else {
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
                     searchRequest: SearchDispatcherRequest,
                     avoidMothershipFor: () => Future[Boolean],
                     mothershipDispatcherFn: Request => Future[ResponseBuilder] = mothershipDispatcher.dispatch): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      avoidMothershipFor().flatMap { isActive =>
          if (isActive) {

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
                  case Some(info) => render.json(info)
                  case _ => render.notFound
                } handle {
                  case RepositoryException(BadRequestStatus, _) =>
                    render.badRequest
                }

              case _ => Future.value(render.badRequest)
            }
          }.map(_.header("Cache-Control", s"public, max-age=$MaxCacheAge, must-revalidate"))
        else {
          mothershipDispatcherFn(request)
        }
      }
    }
  }
}

object SearchController {
  val MaxCacheAge = 60
  val defaultParams = Set("q")
  val playlistParams = Set("q", "license")
  val trackParams = Set("q", "genres", "tags", "license")
}

