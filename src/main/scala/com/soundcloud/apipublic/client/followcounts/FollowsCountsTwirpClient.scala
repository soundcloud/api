package com.soundcloud.apipublic.client.followcounts

import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.util.Future
import proto.soundcloud.follows.api._
import scalapb.FieldMaskUtil

class FollowsCountsTwirpClient(protoClient: FollowsClientProtobuf) {

  def counts(userUrns: Seq[Urn]): Future[GetTargetCountsBatchResponse] = {
    protoClient.getTargetCountsBatch(
      GetTargetCountsBatchRequest(
        userUrns.map(_.toString),
        FieldMaskUtil.fromFieldNumbers[Metrics](Metrics.FOLLOWS_FIELD_NUMBER, Metrics.FOLLOWEDBY_FIELD_NUMBER)
      )
    )
  }
}
