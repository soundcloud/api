package com.soundcloud.apipublic.client.followcounts

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.config.InMemoryConfig
import com.twitter.util.{Await, Future}
import org.specs2.mutable.Before
import proto.soundcloud.follows.api.{GetTargetCountResponse, GetTargetCountsBatchResponse, Metrics}

class FollowCountsClientSpec extends UnitSpecification {
  trait Context extends Scope with Before {
    lazy val jsonClient = mock[JsonClient]
    lazy val twirpClient = mock[FollowsCountsTwirpClient]
    lazy val rollout = mock[Rollout]
    lazy val user = Urn("soundcloud", "users", "1")

    val config = new InMemoryConfig
    config.set("FOLLOW_COUNTS_FETCH_MAX_ENTRIES", "10")

    lazy val client = new FollowCountsClient(twirpClient, config)

    lazy val result = Await.result(client.counts(Seq(user)))
  }

  trait TwirpContext extends Context {
    def response: Future[GetTargetCountsBatchResponse]
    override def before: Any = {
      twirpClient.counts(Seq(user)) returns response
    }
  }

  "#counts" >> {
    "returns counts on successful response" in new TwirpContext {
      override def response =
        Future.value(
          GetTargetCountsBatchResponse(
            Seq(
              GetTargetCountResponse(
                userUrn = user.toString,
                metric = Some(Metrics(followedBy = 10, follows = 20))
              )
            )
          )
        )

      there were noCallsTo(jsonClient)
      result ==== Seq(FollowCounts(user, 10, 20))
    }

    "returns empty set when server responds with an exception" in new TwirpContext {
      override def response = Future.exception(new Exception())

      there were noCallsTo(jsonClient)
      result ==== Seq.empty
    }
  }
}
