package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.Future
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.features.Rollout
import com.soundcloud.publicApiStrangler.mapper.search.SearchMapper
import com.soundcloud.publicApiStrangler.mapping.search.SearchDispatcherRequest
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.BadRequestStatus
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Return, Try}

/**
 * Redirects search queries on to search-dispatcher and fetches meta data.
 */
class SearchController(userAuthentication: UserAuthentication,
                       searchMapper: SearchMapper,
                       baseUrl: String,
                       rollout: Rollout,
                       fallback: DispatchToMothershipHandler)
  extends BffInjectionBasedController {

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

  private def dispatchUserRequest = dispatchRequest(
    Set("q"),
    SearchDispatcherRequest.userSearch,
    "search_avoid_mothership_for_users"
  )

  private def dispatchGroupRequest = dispatchRequest(
    Set("q"),
    SearchDispatcherRequest.groupSearch,
    "search_avoid_mothership_for_groups"
  )

  private def dispatchPlaylistRequest = dispatchRequest(
    Set("q", "license"),
    SearchDispatcherRequest.playlistSearch,
    "search_avoid_mothership_for_playlists"
  )

  private def dispatchTrackRequest = dispatchRequest(
    Set("q", "genres", "tags", "license"),
    SearchDispatcherRequest.trackSearch,
    "search_avoid_mothership_for_tracks"
  )

  /**
   * Perform a search for tracks. Logic to determine whether this is a search
   * and if we should forward the request to Mothership.
   */
  private def dispatchRequest(searchParams: Set[String], makeRequest: Request => SearchDispatcherRequest, featureName: String): BffRequestHandler = request =>
    if (isSearchRequest(request.params, searchParams)) search(request, makeRequest(request), featureName)
    else fallback.dispatch(request)

  private def isSearchRequest(params: ParamMap, searchParams: Set[String]): Boolean = {
    val paramsWithContent = params.collect { case (k, v) if v != null && v.nonEmpty => k}.toSet
    (searchParams intersect paramsWithContent).nonEmpty
  }

  private def validateParam(request: Request, param: String, pred: Int => Boolean) =
    request.params.get(param) match {
      case Some(value) => Try(value.toInt).map(pred)
      case _ => Return(true)
    }

  private def search(request: Request, searchRequest: SearchDispatcherRequest, featureName: String): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      if (rollout.isActive(featureName)) {

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
      }
      else {
        fallback.dispatch(request)
      }
    }
  }
}
