package com.soundcloud.publicApiStrangler.handler.search

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome.Outcome
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.search.SearchHandler._
import com.soundcloud.publicApiStrangler.service.SearchService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackPagination
import com.twitter.finagle.http.{ParamMap, Response}
import com.twitter.util.Future
import play.api.libs.json.Writes

import java.net.URL

/**
  * Redirects search queries on to search-dispatcher and fetches meta data.
  */
class SearchHandler(
    userAuthentication: UserAuthentication,
    baseUrl: String,
    searchService: SearchService,
    telemetry: Telemetry
) {
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
    addWildcardIfNoSearchQuery(req, defaultParams, searchUsers)

  def searchPlaylists(req: HandlerRequest): Future[Response] =
    addWildcardIfNoSearchQuery(req, defaultParams, searchPlaylists)

  def searchTracks(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      val hasLinkedPartitioning = req.params.contains("linked_partitioning")
      val pagination = TrackPagination.fromRequest(req.params, new URL(baseUrl + req.uri))

      val tracksCollection = searchService
        .searchTracks(session, req.params, pagination)
        .value
        .onSuccess(recordIncompleteResponses(_, pagination.limit))

      CollectionResponse.handleCollectionResponse(tracksCollection, hasLinkedPartitioning)
    }
  }

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

  private def searchPlaylists(
      req: HandlerRequest,
      session: UserSession,
      extraParams: Option[ParamMap]
  ): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = OffsetBasedPagination.build(req, Seq("linked_partitioning") ++ searchService.playlistParams)

    val params = ParamMap(extraParams.map(_ ++ req.params).getOrElse(req.params))

    val playlistsCollection =
      searchService
        .searchPlaylists(session, params, pagination)
        .value
    CollectionResponse.handleCollectionResponse(playlistsCollection, hasLinkedPartitioning)

  }

  private def searchUsers(
      req: HandlerRequest,
      session: UserSession,
      extraParams: Option[ParamMap]
  ): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = OffsetBasedPagination.build(req, Seq("linked_partitioning") ++ searchService.userParams)

    val params = extraParams.map(_ ++ req.params).getOrElse(req.params)
    val usersCollection =
      searchService
        .searchUsers(session, params, pagination)
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
