package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserMapper
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.search.{Doc, SearchClient, SearchResponse}
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.{PlaylistBuilder, PlaylistRequest}
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.JsObject

class SearchServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {
    val playlist = new PlaylistBuilder().build
    var user = Fixtures.okidokiUsers.as[List[JsObject]].map(UserMapper(_)).head
    user = user.copy(reposts_count = Some(500L))

    val trackRepresentationsService = mock[TrackRepresentationsService]

    val playlistsService = mock[PlaylistsService]
    val searchClient = mock[SearchClient]
    val trackPagination = mock[TrackPagination]
    val offsetBasedPagination = mock[OffsetBasedPagination]
    val followCountsClient = mock[FollowCountsClient]
    val repostsClient = mock[RepostsClient]
    val okidokiClient = mock[OkidokiClient]

    val trackRepresentationMock = createTrackRepresentation
    val searchService = new SearchService(
      searchClient,
      trackRepresentationsService,
      followCountsClient,
      repostsClient,
      playlistsService,
      okidokiClient
    )

    val query = "foo"
    val queryUrn = Urn("soundcloud", "search", "foo")
    val playlistUrn = Urn("soundcloud", "playlists", playlist.id.toString)
    val userUrn = user.urn

  }

  "#searchUsers" >> {
    "when all data is available" in new Context {
      when(
        searchClient.searchUsers(
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
          Seq(Doc(userUrn)),
          None
        ).goodF
      )

      when(followCountsClient.counts(session, Seq(userUrn)))
        .thenReturn(Future.value(Seq(FollowCounts(userUrn = userUrn, followers = 20976L, followings = 118L))))

      when(repostsClient.getRepostCountsByUrnWithFallback(session, Set(userUrn)))
        .thenReturn(Future.value(Map(userUrn -> 500L)))

      when(okidokiClient.fetch(session, Set(userUrn)))
        .thenReturn(Future.value(Fixtures.okidokiUsers.as[List[JsObject]]))

      val result = Await.result(searchService.searchUsers(session, Map("q" -> query), offsetBasedPagination).value)
      val usersCollection = result.getOrElse(Collection(List.empty, None))
      usersCollection.items ==== List(user)
    }

    "when data is not available" in new Context {
      when(
        searchClient.searchUsers(
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
          Seq.empty,
          None
        ).goodF
      )

      when(followCountsClient.counts(session, Seq.empty))
        .thenReturn(Future.value(Seq.empty))

      // TODO: figure out why empty map doesn't work
      when(repostsClient.getRepostCountsByUrnWithFallback(session, Set.empty))
        .thenReturn(Future.value(Map(userUrn -> 500L)))

      when(okidokiClient.fetch(session, Set.empty))
        .thenReturn(Future.value(List.empty))

      val result = Await.result(searchService.searchUsers(session, Map("q" -> query), offsetBasedPagination).value)

      val usersCollection = result.getOrElse(Collection(Nil, None))
      usersCollection.items ==== List.empty
    }
  }

  "#searchTracks" >> {
    "when all data is available" in new Context {
      when(
        trackRepresentationsService.tracks(
          session,
          List(TrackRequest(trackRepresentationMock.visibleTrack.urn, None)),
          AccessParams.defaultAccess
        )
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

      val result = Await.result(searchService.searchTracks(session, ParamMap(("q", query)), trackPagination).value)

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List(trackRepresentationMock)
    }

    "when data is not available" in new Context {
      when(
        trackRepresentationsService.tracks(session, List.empty, AccessParams.defaultAccess)
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

      val result = Await.result(searchService.searchTracks(session, ParamMap(("q", query)), trackPagination).value)

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List.empty
    }
  }

  "#searchPlaylists" >> {
    "when all data is available" in new Context {
      when(
        playlistsService.fetchPlaylists(session, List(PlaylistRequest(playlistUrn, None)), Some(offsetBasedPagination))
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

      val result = Await.result(searchService.searchPlaylists(session, Map("q" -> query), offsetBasedPagination).value)

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List(playlist)
    }

    "when data is not available" in new Context {
      when(
        playlistsService.fetchPlaylists(session, List.empty, Some(offsetBasedPagination))
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

      val result = Await.result(searchService.searchPlaylists(session, Map("q" -> query), offsetBasedPagination).value)

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List.empty
    }
  }
}
