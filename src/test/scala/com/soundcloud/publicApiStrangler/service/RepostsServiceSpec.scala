package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.{Deleted, Failed, NotFound}
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
    val rollout = mock[Rollout]
    val repostsService = new RepostsService(userRepresentationService, repostsClient, trackRepostsService, rollout)

    val trackUrn = Urn("soundcloud", "tracks", "123")
    val session = new UserSessionBuilder().build()
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)
    val response = RepostTrackResponse()

    trackRepostsService.deleteTrackRepost(request) returns Future.value(response)
    rollout.isActive(any[RolloutFeature]) returns Future.True
  }

  "delete track repost" >> {
    "returns Deleted when a track repost is deleted" in new Context {
      Await.result(repostsService.deleteTracksRepost(session, trackUrn)) ==== Deleted
    }

    "returns NotFound when a track repost is not found" in new Context {
      trackRepostsService.deleteTrackRepost(request) returns Future.exception(TwinagleException(ErrorCode.NotFound, ""))

      Await.result(repostsService.deleteTracksRepost(session, trackUrn)) ==== NotFound
    }

    "returns Failed when a track repost deletion failed" in new Context {
      trackRepostsService.deleteTrackRepost(request) returns Future.exception(TwinagleException(ErrorCode.Internal, ""))

      Await.result(repostsService.deleteTracksRepost(session, trackUrn)) ==== Failed
    }
  }

}
