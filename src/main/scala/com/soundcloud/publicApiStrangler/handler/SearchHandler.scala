package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.bff.nextbff.UntypedJson
import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.bff.nextbff.repository.RepositoryException
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.{Counter, Telemetry}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.handler.SearchHandler._
import com.soundcloud.publicApiStrangler.handler.representation.tracks.TrackRepresentationResponse.handleTracksCollectionResponseFromService
import com.soundcloud.publicApiStrangler.mapper.search.{SearchDispatcherRequest, SearchMapper}
import com.soundcloud.publicApiStrangler.service.SearchService
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackPagination, TracksCollection}
import com.twitter.finagle.http.{ParamMap, Response, Status}
import com.twitter.util.{Future, Return, Try}
import com.soundcloud.outcome._

/**
  * Redirects search queries on to search-dispatcher and fetches meta data.
  */
class SearchHandler(
    userAuthentication: UserAuthentication,
    mothershipDispatcher: DispatchToMothershipHandler,
    mothershipCounter: Counter,
    followCountsClient: FollowCountsClient,
    searchMapper: SearchMapper,
    baseUrl: String,
    lieblingClient: LieblingClient,
    userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher,
    searchService: SearchService,
    telemetry: Telemetry
) {
  private val resourceRequestsCounter = telemetry.counter(
    "top_level_resource_requests_total",
    "Number of entity requests by type",
    "request_type",
    "resource_type",
    "client_id"
  )

  private val requestsParamsCounter = telemetry.counter(
    "top_level_resource_filter_request_params_total",
    "Number of requested params for filter requests by type",
    "resource_type",
    "client_id",
    "param"
  )

  private val requestsWithoutParamsCounter = telemetry.counter(
    "top_level_resource_filter_request_without_params_total",
    "Number of entity requests without params by type",
    "resource_type",
    "client_id"
  )

  def dispatchUserRequest = dispatchRequest(
    defaultParams,
    SearchDispatcherRequest.userSearch,
    "user",
    userRelatedMothershipDispatcher.dispatchToMothership _
  )

  def dispatchPlaylistRequest = dispatchRequest(
    playlistParams,
    SearchDispatcherRequest.playlistSearch,
    "playlist"
  )

  def searchTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      performSearchTracks(req, session)
    }
  }

  private def performSearchTracks(req: HandlerRequest, session: UserSession): Future[Response] = {
    val hasLinkedPartitioning = req.params.get("linked_partitioning").isDefined
    val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

    def fetchTracksRepresentation(params: Map[String, String]): Future[Outcome[TracksCollection]] = {
      searchService
        .searchTracks(session, params, pagination)
        .map(Good(_))
    }
    val trackRepresentation = fetchTracksRepresentation(req.params)
    handleTracksCollectionResponseFromService(trackRepresentation, hasLinkedPartitioning)

  }

  /**
    * Perform a search for a given entity. Logic to determine whether this is a search
    * and if we should forward the request to Mothership.
    */
  private def dispatchRequest(
      searchParams: Set[String],
      makeRequest: HandlerRequest => SearchDispatcherRequest,
      resourceType: String,
      mothershipDispatcherFn: Handler = mothershipDispatcher.dispatch
  ): Handler = { request =>
    userAuthentication
      .withUserSession(request) { session =>
        val clientAppId = Option(session.getAgent).map(_.identifier).getOrElse("unknown")

        if (isSearchRequest(request.params, searchParams)) {
          resourceRequestsCounter.labels("search", resourceType, clientAppId).inc()
          search(request, session, makeRequest(request))
        } else {
          SoundCloudLoggerFactory
            .getLogger(getClass)
            .warn(s"Top Level Entty request, params: ${request.params.toString()}, appId: ${clientAppId}")

          resourceRequestsCounter.labels("filter", resourceType, clientAppId).inc()

          if (allowedFilters.forall(filterKey => !request.params.keySet.contains(filterKey))) {
            requestsWithoutParamsCounter.labels(resourceType, clientAppId).inc()
          }
          allowedFilters.foreach { filterKey =>
            if (request.params.keySet.contains(filterKey)) {
              requestsParamsCounter.labels(resourceType, clientAppId, filterKey).inc()
            }
          }

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
