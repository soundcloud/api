package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.{Created, Deleted, Failed, Forbidden, NotFound}
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.{Await, Future}
import proto.soundcloud.tracks.api.{RepostTrackRequest, RepostTrackResponse, RepostsService => TrackRepostsService}

class RepostsServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val userRepresentationService = mock[UserRepresentationsService]
    val repostsClient = mock[RepostsClient]
    val trackRepostsService = mock[TrackRepostsService]
    val repostsService = new RepostsService(userRepresentationService, repostsClient, trackRepostsService)

    val trackUrn = Urn("soundcloud", "tracks", "123")
    val session = new UserSessionBuilder().build()
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)
    val response = RepostTrackResponse()
  }

  "create track repost" >> {
    trait CreateContext extends Context {
      trackRepostsService.repostTrack(request) returns Future.value(response)
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
      trackRepostsService.deleteTrackRepost(request) returns Future.value(response)
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

}
