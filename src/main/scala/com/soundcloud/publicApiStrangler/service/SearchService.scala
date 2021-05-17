package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.http.client.{Params, StringParam}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.search.SearchClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParamsExtractor
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistRequest
import com.soundcloud.publicApiStrangler.service.playlists.representation.Playlist
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentation,
  TrackRepresentationsService
}
import com.soundcloud.publicApiStrangler.service.users.UserOrderingUtils.sortByProvidedUrns
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.twitter.finagle.http.ParamMap

class SearchService(
    searchClient: SearchClient,
    trackRepresentationsService: TrackRepresentationsService,
    playlistsService: PlaylistsService,
    userRepresentationsService: UserRepresentationsService
) {
  def searchUsers(
      session: UserSession,
      params: Map[String, String],
      pagination: OffsetBasedPagination
  ): OutcomeF[Collection[UserRepresentation]] = {
    val mapParams = mapUserParams(params)

    for {
      searchPage <- searchClient.searchUsers(session, mapParams)
      userUrns = searchPage.docs.map(_.urn).toSet
      users <- userRepresentationsService.getUsers(session, userUrns).outcomeF
      sortedUsers = sortByProvidedUrns(users, searchPage.docs.map(_.urn))
    } yield Collection[UserRepresentation](sortedUsers.toList, pagination.nextHref(searchPage.total_results.toInt))
  }

  def searchTracks(
      session: UserSession,
      params: ParamMap,
      trackPagination: TrackPagination
  ): OutcomeF[Collection[TrackRepresentation]] = {
    val access = AccessParamsExtractor.unapply(params)
    // to keep current behavior, we only fetch free tracks if no access filter defined
    val contentTier = if (params.contains("access")) "ANY" else "FREE"
    val mapParams = mapTrackParams(params) ++ Params(
      "filter.content_tier" -> contentTier,
      "filter.content_country" -> session.getGeo.getCountryCode
    )

    for {
      searchPage <- searchClient.searchTracks(session, mapParams)
      enrichedTracks <- trackRepresentationsService
        .tracks(session, searchPage.docs.map(doc => TrackRequest(doc.urn, None)).toList, access)
        .outcomeF
    } yield {
      Collection(enrichedTracks, trackPagination.nextHref(searchPage.total_results.toInt))
    }
  }

  def searchPlaylists(
      session: UserSession,
      params: ParamMap,
      pagination: OffsetBasedPagination
  ): OutcomeF[Collection[Playlist]] = {
    val mapParams = mapPlaylistParams(params)
    val access = AccessParamsExtractor.unapply(params)

    for {
      searchPage <- searchClient.searchPlaylists(session, mapParams)
      playlistRequests = searchPage.docs.map(doc => PlaylistRequest(urn = doc.urn, None))
      playlists <- playlistsService.fetchPlaylists(session, playlistRequests.toList, access, Some(pagination)).outcomeF
    } yield {
      Collection(playlists, pagination.nextHref(searchPage.total_results.toInt))
    }
  }

  private val TrackParamMappings = Map(
    "q" -> "q",
    "offset" -> "offset",
    "limit" -> "limit",
    "order" -> "sort",
    "tags" -> "filter.tag",
    "license" -> "filter.license",
    "bpm" -> "filter.bpm",
    "bpm[from]" -> "filter.bpm[from]",
    "bpm[to]" -> "filter.bpm[to]",
    "duration" -> "filter.duration",
    "duration[from]" -> "filter.duration[from]",
    "duration[to]" -> "filter.duration[to]",
    "created_at" -> "filter.created_at",
    "created_at[from]" -> "filter.created_at[from]",
    "created_at[to]" -> "filter.created_at[to]",
    "ids" -> "filter.id",
    "genres" -> "filter.genre",
    "client_id" -> "client_id"
  )

  private def mapTrackParams(params: Params): Params = params.collect {
    case (k, v) if TrackParamMappings contains k => TrackParamMappings(k) -> v
    case ("filter", v) if v.value contains "downloadable" => "filter.downloadable" -> StringParam("true")
    case ("filter", v) if v.value contains "streamable" => "filter.streamable" -> StringParam("true")
  }

  private val PlaylistParamMappings = Map(
    "q" -> "q",
    "offset" -> "offset",
    "limit" -> "limit",
    "order" -> "sort",
    "created_at" -> "filter.created_at",
    "ids" -> "filter.id",
    "client_id" -> "client_id",
    "genres" -> "filter.genre",
    "tags" -> "filter.tag"
  )
  val playlistParams: Seq[String] = PlaylistParamMappings.keys.toSeq

  private def mapPlaylistParams(params: Params): Params = params.collect {
    case (k, v) if PlaylistParamMappings contains k => PlaylistParamMappings(k) -> v
  }

  private val UserParamMappings = Map(
    "q" -> "q",
    "offset" -> "offset",
    "limit" -> "limit",
    "order" -> "sort",
    "created_at" -> "filter.created_at",
    "created_at[from]" -> "filter.created_at[from]",
    "created_at[to]" -> "filter.created_at[to]",
    "ids" -> "filter.id",
    "client_id" -> "client_id",
    "place" -> "filter.place"
  )
  val userParams: Seq[String] = UserParamMappings.keys.toSeq

  private def mapUserParams(params: Params): Params = params.collect {
    case (k, v) if UserParamMappings contains k => UserParamMappings(k) -> v
  }
}
