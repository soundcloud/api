package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Counter
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.handler.SearchHandler._
import com.soundcloud.publicApiStrangler.mapper.search.SearchMapper
import com.soundcloud.publicApiStrangler.mapping.search.SearchDispatcherRequest
import com.soundcloud.publicApiStrangler.support.{DispatchToMothershipHandler, UntypedJson}
import com.twitter.finagle.http.{ParamMap, Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.Json

/**
  * Redirects search queries on to search-dispatcher and fetches meta data.
  */
class SearchHandler(userAuthentication: UserAuthentication,
                    mothershipDispatcher: DispatchToMothershipHandler,
                    mothershipCounter: Counter,
                    followCountsClient: FollowCountsClient,
                    searchMapper: SearchMapper,
                    baseUrl: String,
                    lieblingClient: LieblingClient,
                    userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher,
                    trackMothershipDispatcherWithCounts: TrackMothershipDispatcherWithCounts) {

  def dispatchUserRequest = dispatchRequest(
    defaultParams,
    SearchDispatcherRequest.userSearch,
    userRelatedMothershipDispatcher.dispatchToMothership _
  )

  def dispatchPlaylistRequest = dispatchRequest(
    playlistParams,
    SearchDispatcherRequest.playlistSearch
  )


  def dispatchTrackRequest = {
    dispatchRequest(
      trackParams,
      SearchDispatcherRequest.trackSearch,
      trackMothershipDispatcherWithCounts.request _
    )
  }

  /**
    * Perform a search for tracks. Logic to determine whether this is a search
    * and if we should forward the request to Mothership.
    */
  private def dispatchRequest(searchParams: Set[String],
                              makeRequest: HandlerRequest => SearchDispatcherRequest,
                              mothershipDispatcherFn: Handler = mothershipDispatcher.dispatch): Handler = { request =>
    if (isSearchRequest(request.params, searchParams)) {
      search(request, makeRequest(request))
    } else {
      mothershipDispatcherFn(request)
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


  private def search(request: HandlerRequest,
                     searchRequest: SearchDispatcherRequest): Future[Response] = {
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
            case Some(info) => JsonResponseBuilder.ok(UntypedJson.write(info))
            case _ => ResponseBuilder.notFound()
          } handle {
            case RepositoryException(Status.BadRequest, _) =>
              ResponseBuilder.badRequest()
          }

        case _ => Future.value(ResponseBuilder.badRequest())
      }
    }.map(appendCacheHeaders(_))
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
}

