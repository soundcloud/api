package com.soundcloud.apipublic.handler.search

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome.Outcome
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.handler.representation.collection.CollectionResponse
import com.soundcloud.apipublic.handler.search.ParamsExtractor._
import com.soundcloud.apipublic.handler.search.SearchHandler.SearchRateLimits
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.apipublic.service.SearchService
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.TrackPagination
import com.soundcloud.apipublic.support.ErrorResponse
import com.twitter.finagle.http.{ParamMap, Response}
import com.twitter.util.Future
import play.api.libs.json.Writes

import java.net.URL

/**
  * Redirects search queries on to the search api and fetches metadata.
  */
class SearchHandler(
    userAuthentication: UserAuthentication,
    baseUrl: String,
    searchService: SearchService,
    telemetry: Telemetry
) {
  protected val userParams: Seq[String] = Seq(
    "q",
    "offset",
    "limit",
    "order",
    "created_at",
    "created_at[from]",
    "created_at[to]",
    "ids",
    "urns",
    "client_id",
    "place"
  )

  protected val playlistParams: Seq[String] = Seq(
    "q",
    "offset",
    "limit",
    "order",
    "created_at",
    "ids",
    "urns",
    "client_id",
    "genres",
    "tags",
    "show_tracks",
    "access"
  )

  private val incompleteResponsesCounter = telemetry.counter(
    "incomplete_paginated_results_total",
    "Number of requests that get fewer results than requested, even when more are available"
  )
  private val missingResultsCounter = telemetry.histogram(
    "missing_paginated_items_total",
    "Number of items that were missing from paginated requests (due to geoblocking, for example)",
    Seq.empty,
    5d,
    10d,
    25d,
    50d,
    100d,
    200d
  )

  def searchUsers(req: HandlerRequest): Future[Response] =
    addWildcardIfNoSearchQuery(req, SearchRateLimits.defaultParams, searchUsers)

  def searchPlaylists(req: HandlerRequest): Future[Response] =
    addWildcardIfNoSearchQuery(req, SearchRateLimits.defaultParams, searchPlaylists)

  def searchTracks(req: HandlerRequest): Future[Response] =
    addWildcardIfNoSearchQuery(req, SearchRateLimits.defaultParams, searchTracks)

  private def recordIncompleteResponses[T: Writes](
      collectionResponse: Outcome[Collection[T]],
      requestedLimit: Int
  ): Unit = {
    collectionResponse.foreach(coll => {
      if (requestedLimit > coll.items.size && coll.nextHref.isDefined) {
        incompleteResponsesCounter.inc()
        missingResultsCounter.observe(requestedLimit - coll.items.size)
      }
    })
  }

  private def searchTracks(
      req: HandlerRequest,
      session: UserSession,
      extraParams: Option[ParamMap]
  ): Future[Response] = {
    try {
      val hasLinkedPartitioning = req.params.contains("linked_partitioning")
      val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

      val access: AccessParams = AccessParamsExtractor.unapply(req.params)
      // to keep current behavior, we only fetch free tracks if no access filter defined
      val paramsWithAccessFilters: ParamMap =
        if (req.params.contains("access")) {
          req.params
        } else {
          // Build a fresh ParamMap to avoid ClassCastException from casting a generic Map
          ParamMap(
            req.params ++ ParamMap(
              "content_tier" -> "FREE",
              "content_country" -> session.getGeo.getCountryCode
            )
          )
        }

      val params =
        extraParams.map(p => paramsWithAccessFilters ++ p).map(ParamMap.apply).getOrElse(paramsWithAccessFilters)

      val tracksCollection = searchService
        .searchTracks(session, params.asTracksParams, pagination, access)
        .value
        .onSuccess(recordIncompleteResponses(_, pagination.limit))

      CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning)
    } catch {
      case _: IllegalArgumentException => Future.value(ErrorResponse.badRequest())
    }
  }

  private def searchPlaylists(
      req: HandlerRequest,
      session: UserSession,
      extraParams: Option[ParamMap]
  ): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = OffsetBasedPagination.build(baseUrl, req, Seq("linked_partitioning") ++ playlistParams)
    val access: AccessParams = AccessParamsExtractor.unapply(req.params)

    val params = ParamMap(extraParams.map(_ ++ req.params).getOrElse(req.params))

    val playlistsCollection =
      searchService
        .searchPlaylists(session, params.asPlaylistParams, pagination, access)
        .value
    CollectionResponse.handleCollectionResponse(playlistsCollection, hasLinkedPartitioning)
  }

  private def searchUsers(
      req: HandlerRequest,
      session: UserSession,
      extraParams: Option[ParamMap]
  ): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = OffsetBasedPagination.build(baseUrl, req, Seq("linked_partitioning") ++ userParams)
    val access: AccessParams = AccessParamsExtractor.unapply(req.params)

    val params: ParamMap = ParamMap(extraParams.map(_ ++ req.params).getOrElse(req.params))
    val usersCollection =
      searchService
        .searchUsers(session, params.asUsersParams, pagination, access)
        .value

    CollectionResponse.handleCollectionResponse(usersCollection, hasLinkedPartitioning)
  }

  private def addWildcardIfNoSearchQuery(
      request: HandlerRequest,
      searchParams: Set[String],
      search: (HandlerRequest, UserSession, Option[ParamMap]) => Future[Response]
  ): Future[Response] = {
    userAuthentication
      .withUserSession(request) { session =>
        if (containsSearchQuery(request.params, searchParams)) {
          search(request, session, None)
        } else {
          search(request, session, Some(ParamMap("q" -> "*")))
        }
      }
  }

  private def containsSearchQuery(params: ParamMap, searchParams: Set[String]): Boolean = {
    val paramsWithContent = params.collect { case (k, v) if v != null && v.nonEmpty => k }.toSet
    (searchParams intersect paramsWithContent).nonEmpty
  }
}

object SearchHandler {
  object SearchRateLimits {
    val defaultParams = Set("q")
    val playlistParams = Set("q", "license")
    val trackParams = Set("q", "genres", "tags", "license")
  }
}
