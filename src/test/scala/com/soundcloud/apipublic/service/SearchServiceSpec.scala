package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.apipublic.client.search.{Doc, SearchClient, SearchResponse, TracksParams}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.{PlaylistBuilder, PlaylistRequest}
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext
}
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.JsObject
import com.soundcloud.apipublic.handler.search.ParamsExtractor._
import com.soundcloud.apipublic.handler.support.requestParser.AccessParamsExtractor

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

    lazy val params = ParamMap("q" -> query)
    lazy val accessParam: AccessParams = AccessParamsExtractor.unapply(params)
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
          ===(ParamMap("q" -> query).asUsersParams),
          anyObject,
          ===(accessParam)
        )
      ).thenReturn(
        SearchResponse(query, queryUrn, 0, 5, 1, 1000, Seq(Doc(userUrn)), None).goodF
      )

      when(userRepresentationsService.users(session, Seq(userUrn))).thenReturn(Future.value(List(user)))

      val result =
        Await.result(
          searchService.searchUsers(session, params.asUsersParams, offsetBasedPagination, accessParam).value
        )
      val usersCollection = result.getOrElse(Collection(List.empty, None))
      usersCollection.items ==== List(user)
    }

    "when data returns playlists as well" in new Context {
      when(
        searchClient.searchUsers(
          ===(session),
          ===(ParamMap("q" -> query).asUsersParams),
          anyObject,
          ===(accessParam)
        )
      ).thenReturn(
        SearchResponse(query, queryUrn, 0, 5, 1, 1000, Seq(Doc(userUrn), Doc(playlistUrn)), None).goodF
      )

      when(userRepresentationsService.users(session, Seq(userUrn))).thenReturn(Future.value(List(user)))

      val result =
        Await.result(
          searchService.searchUsers(session, params.asUsersParams, offsetBasedPagination, accessParam).value
        )
      val usersCollection = result.getOrElse(Collection(List.empty, None))
      usersCollection.items ==== List(user)
    }

    "when data is not available" in new Context {
      when(
        searchClient.searchUsers(
          ===(session),
          ===(ParamMap("q" -> query).asUsersParams),
          anyObject,
          ===(accessParam)
        )
      ).thenReturn(
        SearchResponse(query, queryUrn, 0, 5, 1, 1000, Seq.empty, None).goodF
      )

      when(userRepresentationsService.users(session, Seq.empty)).thenReturn(Future.value(List.empty))

      val result =
        Await.result(
          searchService.searchUsers(session, params.asUsersParams, offsetBasedPagination, accessParam).value
        )

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
        trackRepresentationsService.tracks(
          ===(session),
          anyObject[List[TrackRequest]],
          anyObject[AccessParams],
          ===(true)
        )
      ).thenReturn(Future.value(tracks))
      when(searchClient.searchTracks(===(session), any[TracksParams], anyObject, ===(accessParam))).thenReturn(response)
    }

    "default to free tier tracks only (to support current behavior)" in new TrackContext {
      val result =
        Await.result(searchService.searchTracks(session, params.asTracksParams, trackPagination, accessParam).value)

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List(trackRepresentationMock)
      there was one(searchClient).searchTracks(
        ===(session),
        ===(ParamMap("q" -> query, "filter.content_tier" -> "FREE", "filter.content_country" -> "--").asTracksParams),
        anyObject,
        ===(accessParam)
      )
    }

    "overrides content tier when access is defined" in new TrackContext {
      override lazy val params = ParamMap(("q", query), ("access", "playable,preview,blocked"))
      val result = Await.result(
        searchService
          .searchTracks(session, params.asTracksParams, trackPagination, accessParam)
          .value
      )

      val tracksCollection = result.getOrElse(Collection(List.empty, None))
      tracksCollection.items ==== List(trackRepresentationMock)
      there was one(searchClient).searchTracks(
        ===(session),
        ===(ParamMap("q" -> query).asTracksParams),
        anyObject,
        ===(accessParam)
      )
    }

    "when data is not available" in new TrackContext {
      override lazy val tracks = List.empty
      override lazy val response = SearchResponse(query, queryUrn, 0, 0, 0, 1000, Seq.empty, None).goodF
      val result =
        Await.result(searchService.searchTracks(session, params.asTracksParams, trackPagination, accessParam).value)

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
          Some(offsetBasedPagination),
          None
        )
      ).thenReturn(Future.value(List(playlist)))
      when(
        searchClient.searchPlaylists(
          ===(session),
          ===(ParamMap("q" -> query).asPlaylistParams),
          anyObject,
          ===(accessParam)
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
        Await.result(
          searchService.searchPlaylists(session, params.asPlaylistParams, offsetBasedPagination, accessParam).value
        )

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List(playlist)
    }

    "when data is not available" in new Context {
      when(
        playlistsService.fetchPlaylists(session, List.empty, access, Some(offsetBasedPagination), None)
      ).thenReturn(Future.value(List.empty))

      when(
        searchClient.searchPlaylists(
          ===(session),
          ===(ParamMap("q" -> query).asPlaylistParams),
          anyObject,
          ===(accessParam)
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
        Await.result(
          searchService.searchPlaylists(session, params.asPlaylistParams, offsetBasedPagination, accessParam).value
        )

      val playlistsCollection = result.getOrElse(Collection(List.empty, None))
      playlistsCollection.items ==== List.empty
    }
  }
}
