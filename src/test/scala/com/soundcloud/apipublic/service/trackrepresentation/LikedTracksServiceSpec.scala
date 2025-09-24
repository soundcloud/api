package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.apipublic.service.likes.LikesComparisonUtil
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import proto.soundcloud.likes.api.v2.{
  AreTargetsLikedByUserRequest,
  AreTargetsLikedByUserResponse,
  LikesService => v2LikesClientProtobuf
}
import proto.soundcloud.likes.api.{IsTargetLikedBatchRequest, IsTargetLikedBatchResponse, LikesClientProtobuf}

class LikedTracksServiceSpec extends UnitSpecification {
  private def likesV2Rollout = RolloutFeature("shadow-likes-v2")
  trait Context extends Scope {
    val likesService = smartMock[LikesClientProtobuf]
    val v2likesService = mock[v2LikesClientProtobuf]
    val exceptionCollector = mock[ExceptionCollector]
    val rollout = mock[Rollout]
    val likesComparisonUtil = mock[LikesComparisonUtil]

    val service =
      new LikedTracksService(likesService, v2likesService, exceptionCollector, rollout, likesComparisonUtil, 2, 2)

    val trackUrn1 = Urn("soundcloud", "tracks", "1")
    val trackUrn2 = Urn("soundcloud", "tracks", "2")
    val trackUrn3 = Urn("soundcloud", "tracks", "3")
    lazy val userUrn = Urn("soundcloud", "users", "1")
    lazy val session = new UserSessionBuilder().setUser(userUrn).build()
    val trackUrns = Seq(trackUrn1, trackUrn2, trackUrn3)
    def mockLikes(tracksRequest: Seq[String], tracksResponse: Seq[String]) =
      when(
        likesService.isTargetLikedBatch(
          IsTargetLikedBatchRequest(
            sourceUrn = userUrn.toString,
            targetUrns = tracksRequest
          )
        )
      ).thenReturn(
        Future.value(
          IsTargetLikedBatchResponse(
            tracksResponse
          )
        )
      )

    def mockLikesV2(tracksRequest: Seq[String], tracksResponse: Seq[String]) =
      when(
        v2likesService.areTargetsLikedByUser(
          AreTargetsLikedByUserRequest(
            userUrn.toString,
            tracksRequest
          )
        )
      ).thenReturn(
        Future.value(
          AreTargetsLikedByUserResponse(
            tracksResponse
          )
        )
      )

    lazy val result = Await.result(service.getLikedTracks(session, trackUrns))
  }

  trait ShadowRolloutV2EnabledContext extends Context {
    rollout.isActive(likesV2Rollout) returns Future.value(true)
  }

  trait ShadowRolloutV2DisabledContext extends Context {
    rollout.isActive(likesV2Rollout) returns Future.value(false)
  }

  "getLikedTracks" >> {
    "returns a map with the tracks that are liked and not liked by the user" in new Context
      with ShadowRolloutV2DisabledContext {
      mockLikes(Seq(trackUrn1.toString, trackUrn2.toString), Seq(trackUrn1.toString))
      mockLikes(Seq(trackUrn3.toString), Seq.empty)

      result ==== Map(trackUrn1 -> true, trackUrn2 -> false, trackUrn3 -> false)
      there was no(v2likesService).areTargetsLikedByUser(any)
    }

    "use likesV2Client when rollout is active" in new Context with ShadowRolloutV2EnabledContext {
      mockLikes(Seq(trackUrn1.toString, trackUrn2.toString), Seq(trackUrn1.toString))
      mockLikes(Seq(trackUrn3.toString), Seq.empty)
      mockLikesV2(Seq(trackUrn1.toString, trackUrn2.toString), Seq(trackUrn1.toString))
      mockLikesV2(Seq(trackUrn3.toString), Seq.empty)

      result ==== Map(trackUrn1 -> true, trackUrn2 -> false, trackUrn3 -> false)

      there was one(v2likesService).areTargetsLikedByUser(
        AreTargetsLikedByUserRequest(
          userUrn.toString,
          Seq(trackUrn1.toString, trackUrn2.toString)
        )
      )

      there was one(likesComparisonUtil).compareAndReportAreLiked(
        "areTargetsLikedByUser",
        userUrn.toString,
        Seq(trackUrn1.toString),
        Seq(trackUrn1.toString)
      )
    }

    "returns result from likes even when V2 errors" in new Context with ShadowRolloutV2EnabledContext {
      mockLikes(Seq(trackUrn1.toString, trackUrn2.toString), Seq(trackUrn1.toString))
      mockLikes(Seq(trackUrn3.toString), Seq.empty)

      when(v2likesService.areTargetsLikedByUser(any))
        .thenReturn(Future.exception(new RuntimeException("test exception")))

      result ==== Map(trackUrn1 -> true, trackUrn2 -> false, trackUrn3 -> false)

      there was one(v2likesService).areTargetsLikedByUser(
        AreTargetsLikedByUserRequest(
          userUrn.toString,
          Seq(trackUrn1.toString, trackUrn2.toString)
        )
      )

      there was one(likesComparisonUtil).compareAndReportAreLiked(
        "areTargetsLikedByUser",
        userUrn.toString,
        Seq(trackUrn1.toString),
        Seq.empty
      )
    }

    "returns an empty map if there is the session is anonymous" in new Context with ShadowRolloutV2DisabledContext {
      override lazy val session = new UserSessionBuilder().build()

      result ==== Map.empty
      there was no(likesService).isTargetLikedBatch(any)
    }

    "returns an empty map if likes request fails and reports the exception" in new Context
      with ShadowRolloutV2DisabledContext {
      when(likesService.isTargetLikedBatch(any)).thenReturn(Future.exception(new RuntimeException("request failed")))

      result ==== Map.empty
      there were two(exceptionCollector).add(any, any, any, any)
    }

    "batches the requests to likes" in new Context with ShadowRolloutV2DisabledContext {
      mockLikes(Seq(trackUrn1.toString, trackUrn2.toString), Seq.empty)
      mockLikes(Seq(trackUrn3.toString), Seq(trackUrn3.toString))

      result ==== Map(trackUrn1 -> false, trackUrn2 -> false, trackUrn3 -> true)
      there were two(likesService).isTargetLikedBatch(any)
    }
  }
}
