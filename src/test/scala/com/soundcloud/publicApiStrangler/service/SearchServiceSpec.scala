package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.search.{Doc, SearchClient, SearchResponse}
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.{PlaylistBuilder, PlaylistRequest}
import com.soundcloud.publicApiStrangler.service.playlists.representation.Collection

class SearchServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {
    val playlist = new PlaylistBuilder().build
    val trackRepresentationsService = mock[TrackRepresentationsService]
    val playlistsService = mock[PlaylistsService]
    val searchClient = mock[SearchClient]
    val trackPagination = mock[TrackPagination]
    val playlistPagination = mock[OffsetBasedPagination]

    val trackRepresentationMock = createTrackRepresentation
    val searchService = new SearchService(
      searchClient,
      trackRepresentationsService,
      playlistsService
    )
    val query = "foo"
    val queryUrn = Urn("soundcloud", "search", "foo")
    val playlistUrn = Urn("soundcloud", "playlists", playlist.id.toString)
  }

  "#searchTracks" >> {
    "when all data is available" in new Context {
      when(
        trackRepresentationsService.tracks(session, List(TrackRequest(trackRepresentationMock.visibleTrack.urn, None)))
      ).thenReturn(Future.value(List(trackRepresentationMock)))
      when(
        searchClient.searchTracks(
          ===(session),
          ===(Params("q" -> query, "filter.content_tier" -> "FREE", "filter.content_country" -> "--")),
          anyObject
        )
      ).thenReturn(
        SearchResponse(
          query,
          queryUrn,
          0,
          5,
          1,
          1000,
          Seq(Doc(trackUrn)),
          None
        ).goodF
      )

      val result = Await.result(searchService.searchTracks(session, Map("q" -> query), trackPagination).value)

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List(trackRepresentationMock)
    }

    "when data is not available" in new Context {
      when(
        trackRepresentationsService.tracks(session, List.empty)
      ).thenReturn(Future.value(List.empty))
      when(
        searchClient.searchTracks(
          ===(session),
          ===(Params("q" -> query, "filter.content_tier" -> "FREE", "filter.content_country" -> "--")),
          anyObject
        )
      ).thenReturn(
        SearchResponse(
          query,
          queryUrn,
          0,
          0,
          0,
          1000,
          Seq.empty,
          None
        ).goodF
      )

      val result = Await.result(searchService.searchTracks(session, Map("q" -> query), trackPagination).value)

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List.empty
    }
  }

  "#searchPlaylists" >> {
    "when all data is available" in new Context {
      when(
        playlistsService.fetchPlaylists(session, List(PlaylistRequest(playlistUrn, None)), Some(playlistPagination))
      ).thenReturn(Future.value(List(playlist)))
      when(
        searchClient.searchPlaylists(
          ===(session),
          ===(Params("q" -> query)),
          anyObject
        )
      ).thenReturn(
        SearchResponse(
          query,
          queryUrn,
          0,
          5,
          1,
          1000,
          Seq(Doc(playlistUrn)),
          None
        ).goodF
      )

      val result = Await.result(searchService.searchPlaylists(session, Map("q" -> query), playlistPagination).value)

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List(playlist)
    }

    "when data is not available" in new Context {
      when(
        playlistsService.fetchPlaylists(session, List.empty, Some(playlistPagination))
      ).thenReturn(Future.value(List.empty))

      when(
        searchClient.searchPlaylists(
          ===(session),
          ===(Params("q" -> query)),
          anyObject
        )
      ).thenReturn(
        SearchResponse(
          query,
          queryUrn,
          0,
          0,
          0,
          1000,
          Seq.empty,
          None
        ).goodF
      )

      val result = Await.result(searchService.searchPlaylists(session, Map("q" -> query), playlistPagination).value)

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List.empty
    }
  }
}
