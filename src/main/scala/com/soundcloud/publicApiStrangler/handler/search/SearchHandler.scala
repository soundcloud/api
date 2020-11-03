package com.soundcloud.publicApiStrangler.handler.search

import java.net.URL

import com.soundcloud.publicApiStrangler.handler.representation.serializers.SearchUserRepresentation.searchUserWrites
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.handler.UserRelatedMothershipDispatcher
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.handler.search.SearchHandler._
import com.soundcloud.publicApiStrangler.service.SearchService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackPagination
import com.twitter.finagle.http.{ParamMap, Response}
import com.twitter.util.Future

/**
  * Redirects search queries on to search-dispatcher and fetches meta data.
  */
class SearchHandler(
    userAuthentication: UserAuthentication,
    baseUrl: String,
    userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher,
    searchService: SearchService
) {
  def dispatchUserRequest = dispatchRequest(
    defaultParams,
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

  def searchUsers(req: HandlerRequest, session: UserSession): Future[Response] = {
    val hasLinkedPartitioning = req.params.contains("linked_partitioning")
    val pagination = OffsetBasedPagination.build(req, Seq("linked_partitioning"))

    val usersCollection =
      searchService
        .searchUsers(session, req.params, pagination)
        .value
    CollectionResponse.handleCollectionResponse(usersCollection, hasLinkedPartitioning)(searchUserWrites)

  }

  /**
    * Perform a search for a given entity. Logic to determine whether this is a search
    * and if we should forward the request to Mothership.
    */
  private def dispatchRequest(
      searchParams: Set[String],
      mothershipDispatcherFn: Handler
  ): Handler = { request =>
    userAuthentication
      .withUserSession(request) { session =>
        if (isSearchRequest(request.params, searchParams)) {
          searchUsers(request, session)
        } else {
          mothershipDispatcherFn(request)
        }
      }
  }

  private def isSearchRequest(params: ParamMap, searchParams: Set[String]): Boolean = {
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
