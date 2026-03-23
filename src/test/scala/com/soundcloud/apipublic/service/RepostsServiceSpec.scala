package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.reposts.RepostsClient.{Created, Deleted, Failed, Forbidden, NotFound}
import com.soundcloud.apipublic.client.reposts.{Reposts, RepostsClient}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.{PlaylistBuilder, PlaylistRequest}
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackRepresentationsService,
  TrackRepresentationSpecContext
}
import com.soundcloud.apipublic.service.users.{UserBuilder, UserRepresentationsService}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.outcome.GoodOps
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import proto.soundcloud.tracks.api.{
  RepostTrackRequest,
  RepostTrackResponse,
  TrackRepostersPagination,
  TrackRepostersRequest,
  TrackRepostersResponse,
  RepostsService => TrackRepostsService
}

class RepostsServiceSpec extends UnitSpecification with TrackRepresentationSpecContext {

  trait Context extends Scope {
    val userRepresentationService = mock[UserRepresentationsService]
    val repostsClient = mock[RepostsClient]
    val trackRepostsService = mock[TrackRepostsService]
    val trackRepresentationsService = mock[TrackRepresentationsService]
    val playlistsService = mock[PlaylistsService]
    val repostsService = new RepostsService(
      userRepresentationService,
      repostsClient,
      trackRepostsService,
      trackRepresentationsService,
      playlistsService
    )

    val trackUrn = Urn("soundcloud", "tracks", "123")
    val session = new UserSessionBuilder().build()
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)
    val expectedResponse = RepostTrackResponse()
  }

  "create track repost" >> {
    trait CreateContext extends Context {
      trackRepostsService.repostTrack(request) returns Future.value(expectedResponse)
    }

    "returns Created when a track repost is deleted" in new CreateContext {
      Await.result(repostsService.createTracksRepost(session, trackUrn)) ==== Created
    }

    "returns NotFound when a track repost is not found" in new CreateContext {
      trackRepostsService.repostTrack(request) returns Future.exception(TwinagleException(ErrorCode.NotFound, ""))

      Await.result(repostsService.createTracksRepost(session, trackUrn)) ==== NotFound
    }

    "returns Failed when a track repost creation failed" in new CreateContext {
      trackRepostsService.repostTrack(request) returns Future.exception(TwinagleException(ErrorCode.Internal, ""))

      Await.result(repostsService.createTracksRepost(session, trackUrn)) ==== Failed
    }

    "returns Forbidden when a track repost creation was not authorized" in new CreateContext {
      trackRepostsService.repostTrack(request) returns Future.exception(
        TwinagleException(ErrorCode.PermissionDenied, "")
      )

      Await.result(repostsService.createTracksRepost(session, trackUrn)) ==== Forbidden
    }
  }

  "delete track repost" >> {
    trait DeleteContext extends Context {
      trackRepostsService.deleteTrackRepost(request) returns Future.value(expectedResponse)
    }

    "returns Deleted when a track repost is deleted" in new DeleteContext {
      Await.result(repostsService.deleteTracksRepost(session, trackUrn)) ==== Deleted
    }

    "returns NotFound when a track repost is not found" in new DeleteContext {
      trackRepostsService.deleteTrackRepost(request) returns Future.exception(TwinagleException(ErrorCode.NotFound, ""))

      Await.result(repostsService.deleteTracksRepost(session, trackUrn)) ==== NotFound
    }

    "returns Failed when a track repost deletion failed" in new DeleteContext {
      trackRepostsService.deleteTrackRepost(request) returns Future.exception(TwinagleException(ErrorCode.Internal, ""))

      Await.result(repostsService.deleteTracksRepost(session, trackUrn)) ==== Failed
    }
  }

  "get tracks reposters" >> {

    trait RepostersContext extends Context {
      val pagination = CursorBasedPagination("http://api.example.com", "", ParamMap(), Some("999"), 10)
      val trackRepostersPagination = TrackRepostersPagination(limit = pagination.pageSize, cursor = pagination.cursor)
      val twirpRequest = TrackRepostersRequest(
        userSession = Some(session.asProtoSession),
        trackUrn = trackUrn.toString,
        pagination = Some(trackRepostersPagination)
      )
      val userUrn = Urn("soundcloud", "users", "123")
      val user = new UserBuilder().setUrn(userUrn).build
    }

    "returns not found when track not accessible" in new RepostersContext {
      trackRepostsService.getTrackReposters(twirpRequest) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "")
      )

      val response = Await.result(repostsService.getTrackReposters(session, trackUrn, pagination))
      response ==== com.soundcloud.jvmkit.module.outcome.NotFound("").bad
    }

    "returns a collection of users" in new RepostersContext {
      trackRepostsService.getTrackReposters(twirpRequest) returns Future.value(
        TrackRepostersResponse(userUrns = Seq(userUrn.toString), cursor = Some("999"))
      )
      userRepresentationService.users(session, Seq(userUrn)) returns Future.value(List(user))

      val response = Await.result(repostsService.getTrackReposters(session, trackUrn, pagination))
      response ==== Collection(List(user), Some("http://api.example.com?cursor=999&page_size=10")).good
    }
  }

  "get track reposts" >> {
    trait TrackRepostsContext extends Context {
      val userIdUrn = Urn("soundcloud", "users", "1")
      val trackPagination = CursorBasedPagination("http://api.example.com", "", ParamMap(), Some("cursor123"), 10)
      val aTrackUrn = Urn("soundcloud", "tracks", "123")
      val trackRepresentation = createTrackRepresentationFromVisibleTrack().copy(urn = aTrackUrn)
    }

    "returns a collection of tracks" in new TrackRepostsContext {
      repostsClient.trackReposts(session, userIdUrn, 10, Some("cursor123")) returns Future.value(
        Reposts(List(aTrackUrn), Some("nextCursor"))
      )
      trackRepresentationsService.tracks(session, List(TrackRequest(aTrackUrn, None)), AccessParams.explicitAccess) returns Future
        .value(List(trackRepresentation))

      val response =
        Await.result(repostsService.getTrackReposts(session, userIdUrn, AccessParams.explicitAccess, trackPagination))
      response ==== Collection(List(trackRepresentation), Some("http://api.example.com?cursor=nextCursor&page_size=10")).good
    }
  }

  "get playlist reposts" >> {
    trait PlaylistRepostsContext extends Context {
      val userIdUrn = Urn("soundcloud", "users", "1")
      val playlistPagination = CursorBasedPagination("http://api.example.com", "", ParamMap(), Some("cursor123"), 10)
      val aPlaylistUrn = Urn("soundcloud", "playlists", "123")
      val playlist = new PlaylistBuilder().build
    }

    "returns a collection of playlists" in new PlaylistRepostsContext {
      repostsClient.playlistReposts(session, userIdUrn, 10, Some("cursor123")) returns Future.value(
        Reposts(List(aPlaylistUrn), Some("nextCursor"))
      )
      playlistsService.fetchPlaylistsMetadataOnly(session, List(PlaylistRequest(aPlaylistUrn, None))) returns Future
        .value(List(playlist))

      val response = Await.result(repostsService.getPlaylistReposts(session, userIdUrn, playlistPagination))
      response ==== Collection(List(playlist), Some("http://api.example.com?cursor=nextCursor&page_size=10")).good
    }
  }

}
