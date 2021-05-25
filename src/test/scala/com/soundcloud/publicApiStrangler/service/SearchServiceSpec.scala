package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserRepresentationMapper
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
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.JsObject

class SearchServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {
    val playlist = new PlaylistBuilder().build
    var user = Fixtures.okidokiUsers.as[List[JsObject]].map(UserRepresentationMapper(_)).head
    user = user.copy(reposts_count = Some(500L))

    val trackRepresentationsService = mock[TrackRepresentationsService]

    val playlistsService = mock[PlaylistsService]
    val searchClient = mock[SearchClient]
    val trackPagination = mock[TrackPagination]
    val offsetBasedPagination = mock[OffsetBasedPagination]
    val userRepresentationsService = mock[UserRepresentationsService]

    val trackRepresentationMock = createTrackRepresentation
    val searchService = new SearchService(
      searchClient,
      trackRepresentationsService,
      playlistsService,
      userRepresentationsService
    )

    val query = "foo"
    val queryUrn = Urn("soundcloud", "search", "foo")
    val playlistUrn = Urn("soundcloud", "playlists", playlist.id.toString)
    val userUrn = user.urn
    val access = AccessParams.defaultAccess
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
        SearchResponse(query, queryUrn, 0, 5, 1, 1000, Seq(Doc(userUrn)), None).goodF
      )

      when(userRepresentationsService.getUsers(session, Set(userUrn))).thenReturn(Future.value(List(user)))

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
        SearchResponse(query, queryUrn, 0, 5, 1, 1000, Seq.empty, None).goodF
      )

      when(userRepresentationsService.getUsers(session, Set.empty)).thenReturn(Future.value(List.empty))

      val result = Await.result(searchService.searchUsers(session, Map("q" -> query), offsetBasedPagination).value)

      val usersCollection = result.getOrElse(Collection(Nil, None))
      usersCollection.items ==== List.empty
    }
  }

  "#searchTracks" >> {
    trait TrackContext extends Context {
      lazy val response: OutcomeF[SearchResponse] =
        SearchResponse(query, queryUrn, 0, 5, 1, 1000, Seq(Doc(trackUrn)), None).goodF
      lazy val tracks = List(trackRepresentationMock)

      when(
        trackRepresentationsService.tracks(===(session), anyObject[List[TrackRequest]], anyObject[AccessParams])
      ).thenReturn(Future.value(tracks))
      when(searchClient.searchTracks(===(session), anyObject, anyObject)).thenReturn(response)
    }

    "default to free tier tracks only (to support current behavior)" in new TrackContext {
      val result = Await.result(searchService.searchTracks(session, ParamMap(("q", query)), trackPagination).value)

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List(trackRepresentationMock)
      there was one(searchClient).searchTracks(
        ===(session),
        ===(Params("q" -> query, "filter.content_tier" -> "FREE", "filter.content_country" -> "--")),
        anyObject
      )
    }

    "overrides content tier when access is defined" in new TrackContext {
      val result = Await.result(
        searchService
          .searchTracks(session, ParamMap(("q", query), ("access", "playable,preview,blocked")), trackPagination)
          .value
      )

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List(trackRepresentationMock)
      there was one(searchClient).searchTracks(
        ===(session),
        ===(Params("q" -> query)),
        anyObject
      )
    }

    "when data is not available" in new TrackContext {
      override lazy val tracks = List.empty
      override lazy val response = SearchResponse(query, queryUrn, 0, 0, 0, 1000, Seq.empty, None).goodF
      val result = Await.result(searchService.searchTracks(session, ParamMap(("q", query)), trackPagination).value)

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List.empty
    }
  }

  "#searchPlaylists" >> {
    "when all data is available" in new Context {
      when(
        playlistsService.fetchPlaylists(
          session,
          List(PlaylistRequest(playlistUrn, None)),
          access,
          Some(offsetBasedPagination)
        )
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

      val result =
        Await.result(searchService.searchPlaylists(session, ParamMap("q" -> query), offsetBasedPagination).value)

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List(playlist)
    }

    "when data is not available" in new Context {
      when(
        playlistsService.fetchPlaylists(session, List.empty, access, Some(offsetBasedPagination))
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

      val result =
        Await.result(searchService.searchPlaylists(session, ParamMap("q" -> query), offsetBasedPagination).value)

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List.empty
    }
  }
}
