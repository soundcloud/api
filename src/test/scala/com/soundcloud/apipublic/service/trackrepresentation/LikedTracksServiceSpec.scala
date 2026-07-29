package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.periskop.client.Severity.Info
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import proto.soundcloud.likes.api.v2.{
  AreTargetsLikedByUserRequest,
  AreTargetsLikedByUserResponse,
  LikesService => v2LikesClientProtobuf
}

class LikedTracksServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    val v2likesService = mock[v2LikesClientProtobuf]
    val exceptionCollector = mock[ExceptionCollector]

    val service =
      new LikedTracksService(v2likesService, exceptionCollector, 2)

    val trackUrn1 = Urn("soundcloud", "tracks", "1")
    val trackUrn2 = Urn("soundcloud", "tracks", "2")
    val trackUrn3 = Urn("soundcloud", "tracks", "3")
    lazy val userUrn = Urn("soundcloud", "users", "1")
    lazy val session = new UserSessionBuilder().setUser(userUrn).build()
    val trackUrns = Seq(trackUrn1, trackUrn2, trackUrn3)

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

  "getLikedTracks" >> {
    "returns a map with the tracks that are liked and not liked by the user" in new Context {
      mockLikesV2(Seq(trackUrn1.toString, trackUrn2.toString), Seq(trackUrn1.toString))
      mockLikesV2(Seq(trackUrn3.toString), Seq.empty)

      result ==== Map(trackUrn1 -> true, trackUrn2 -> false, trackUrn3 -> false)

      there was one(v2likesService).areTargetsLikedByUser(
        AreTargetsLikedByUserRequest(
          userUrn.toString,
          Seq(trackUrn1.toString, trackUrn2.toString)
        )
      )
    }

    "returns an empty map if there is the session is anonymous" in new Context {
      override lazy val session = new UserSessionBuilder().build()

      result ==== Map.empty
      there was no(v2likesService).areTargetsLikedByUser(any)
    }

    "returns an all-false map if likes request fails and reports the exception" in new Context {
      when(v2likesService.areTargetsLikedByUser(any))
        .thenReturn(Future.exception(new RuntimeException("request failed")))

      result ==== Map(trackUrn1 -> false, trackUrn2 -> false, trackUrn3 -> false)
      there were two(exceptionCollector).addMessage(
        "v2likes_areTargetsLikedByUser",
        "error fetching from likes v2",
        Info,
        collectRequestBody = true
      )
    }

    "batches the requests to likes" in new Context {
      mockLikesV2(Seq(trackUrn1.toString, trackUrn2.toString), Seq.empty)
      mockLikesV2(Seq(trackUrn3.toString), Seq(trackUrn3.toString))

      result ==== Map(trackUrn1 -> false, trackUrn2 -> false, trackUrn3 -> true)
      there were two(v2likesService).areTargetsLikedByUser(any)
    }
  }
}
