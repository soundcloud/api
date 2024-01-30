package com.soundcloud.apipublic.client.followcounts

import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import proto.soundcloud.follows.api.{
  FollowsClientProtobuf,
  GetTargetCountsBatchRequest,
  GetTargetCountsBatchResponse,
  Metrics
}
import scalapb.FieldMaskUtil

class FollowsCountsTwirpClientSpec extends Specification with Mockito {
  trait Context extends Scope {
    val protoClient = mock[FollowsClientProtobuf]
    val urns = Seq(Urn("soundcloud", "users", "1"))
    val client = new FollowsCountsTwirpClient(protoClient)

    protoClient.getTargetCountsBatch(any()) returns Future.value(GetTargetCountsBatchResponse(Seq()))
  }

  "#counts" >> {
    "it sends a request to the follows protobuf client" in new Context {
      Await.result(client.counts(urns))

      verify(protoClient, times(1)).getTargetCountsBatch(
        GetTargetCountsBatchRequest(
          urns.map(_.toString),
          FieldMaskUtil.fromFieldNumbers[Metrics](Metrics.FOLLOWS_FIELD_NUMBER, Metrics.FOLLOWED_BY_FIELD_NUMBER)
        )
      )
    }
  }
}
