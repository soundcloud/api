package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import proto.soundcloud.likes.api.{IsTargetLikedBatchRequest, IsTargetLikedBatchResponse, LikesClientProtobuf}

class LikedTracksServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    val likesService = smartMock[LikesClientProtobuf]
    val exceptionCollector = mock[ExceptionCollector]
    val service = new LikedTracksService(likesService, exceptionCollector, 2)

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

    lazy val result = Await.result(service.getLikedTracks(session, trackUrns))
  }
  "getLikedTracks" >> {
    "returns a map with the tracks that are liked and not liked by the user" in new Context {
      mockLikes(Seq(trackUrn1.toString, trackUrn2.toString), Seq(trackUrn1.toString))
      mockLikes(Seq(trackUrn3.toString), Seq.empty)

      result ==== Map(trackUrn1 -> true, trackUrn2 -> false, trackUrn3 -> false)
    }

    "returns an empty map if there is the session is anonymous" in new Context {
      override lazy val session = new UserSessionBuilder().build()

      result ==== Map.empty
      there was no(likesService).isTargetLikedBatch(any)
    }

    "returns an empty map if likes request fails and reports the exception" in new Context {
      when(likesService.isTargetLikedBatch(any)).thenReturn(Future.exception(new RuntimeException("request failed")))

      result ==== Map.empty
      there were two(exceptionCollector).add(any, any, any, any)
    }

    "batches the requests to likes" in new Context {
      mockLikes(Seq(trackUrn1.toString, trackUrn2.toString), Seq.empty)
      mockLikes(Seq(trackUrn3.toString), Seq(trackUrn3.toString))

      result ==== Map(trackUrn1 -> false, trackUrn2 -> false, trackUrn3 -> true)
      there were two(likesService).isTargetLikedBatch(any)
    }
  }
}
