package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.search.{PlaylistsParams, SearchClient, TracksParams, UsersParams}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.PlaylistRequest
import com.soundcloud.apipublic.service.playlists.representation.Playlist
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentation,
  TrackRepresentationsService
}
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}

class SearchService(
    searchDispatcherClient: SearchClient,
    searchApiClient: SearchClient,
    trackRepresentationsService: TrackRepresentationsService,
    playlistsService: PlaylistsService,
    userRepresentationsService: UserRepresentationsService,
    rolloutClient: Rollout
) {

  private val useSearchSDUIApi: BasicRolloutFeature = BasicRolloutFeature("use-search-sdui-api")

  private def client: OutcomeF[SearchClient] =
    rolloutClient
      .isActive(useSearchSDUIApi)
      .map {
        case true => searchApiClient
        case false => searchDispatcherClient
      }
      .outcomeF

  def searchUsers(
      session: UserSession,
      params: UsersParams,
      pagination: OffsetBasedPagination,
      access: AccessParams
  ): OutcomeF[Collection[UserRepresentation]] =
    for {
      searchClient <- client
      searchPage <- searchClient.searchUsers(session = session, params = params, access = access)
      userUrns = searchPage.docs.map(_.urn).toSet
      users <- userRepresentationsService.users(session, userUrns.toSeq).outcomeF
    } yield Collection[UserRepresentation](users, pagination.nextHref(searchPage.total_results.toInt))

  def searchTracks(
      session: UserSession,
      params: TracksParams,
      trackPagination: TrackPagination,
      access: AccessParams
  ): OutcomeF[Collection[TrackRepresentation]] = {
    for {
      searchClient <- client
      searchPage <- searchClient.searchTracks(session = session, params = params, access = access)
      enrichedTracks <- trackRepresentationsService
        .tracks(session, searchPage.docs.map(doc => TrackRequest(doc.urn, None)).toList, access)
        .outcomeF
    } yield {
      Collection(enrichedTracks, trackPagination.nextHref(searchPage.total_results.toInt))
    }
  }

  def searchPlaylists(
      session: UserSession,
      params: PlaylistsParams,
      pagination: OffsetBasedPagination,
      access: AccessParams
  ): OutcomeF[Collection[Playlist]] = {
    for {
      searchClient <- client
      searchPage <- searchClient.searchPlaylists(session = session, params = params, access = access)
      playlistRequests = searchPage.docs.map(doc => PlaylistRequest(urn = doc.urn, None))
      playlists <- playlistsService
        .fetchPlaylists(session, playlistRequests.toList, access, Some(pagination), params.showTracks)
        .outcomeF
    } yield {
      Collection(playlists, pagination.nextHref(searchPage.total_results.toInt))
    }
  }

}
