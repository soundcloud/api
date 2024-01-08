package com.soundcloud.apipublic.client.followcounts

import java.net.URLEncoder
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.config.InMemoryConfig
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures.withContentsOf
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before
import play.api.libs.json.JsNull
import proto.soundcloud.follows.api.{GetTargetCountResponse, GetTargetCountsBatchResponse, Metrics}

class FollowCountsClientSpec extends UnitSpecification {
  trait Context extends Scope with Before {
    lazy val jsonClient = mock[JsonClient]
    lazy val twirpClient = mock[FollowsCountsTwirpClient]
    lazy val rollout = mock[Rollout]
    lazy val user = Urn("soundcloud", "users", "1")

    val config = new InMemoryConfig
    config.set("FOLLOW_COUNTS_FETCH_MAX_ENTRIES", "10")

    lazy val client = new FollowCountsClient(jsonClient, twirpClient, Some(rollout), config)

    lazy val result = Await.result(client.counts(anonymousSession, Seq(user)))
  }

  trait RolloutDisabledContext extends Context {
    rollout.isActive(client.followsCountsFeature) returns Future.False

    def response: Future[Response]
    override def before: Any = {
      val bulkParams = Params(
        "followingCounts" -> URLEncoder.encode(
          s"/timeseries?resolution=alltime&category=f.u&minus-category=n.f.u&keys=${user.identifier}",
          "UTF-8"
        ),
        "followerCounts" -> URLEncoder.encode(
          s"/timeseries?resolution=alltime&category=f.b.u&minus-category=n.f.b.u&keys=${user.identifier}",
          "UTF-8"
        )
      )

      when(jsonClient.getWithSession(anonymousSession, Path() / "bulk", bulkParams, Headers.empty)) thenReturn response
    }
  }

  trait RolloutEnabledContext extends Context {
    rollout.isActive(client.followsCountsFeature) returns Future.True
    def response: Future[GetTargetCountsBatchResponse]
    override def before: Any = {
      twirpClient.counts(Seq(user)) returns response
    }
  }

  "rollout disabled" >> {
    "returns counts on successful response" in new RolloutDisabledContext {
      override def response =
        Future.value(jsonResponse(Status.Ok, withContentsOf("stitch4follows", "bulk_follow_counts_response")))

      there were noCallsTo(twirpClient)
      result ==== Seq(FollowCounts(user, 10, 20))
    }

    "returns empty set when server responds with a non OK status" in new RolloutDisabledContext {
      override def response = Future.value(jsonResponse(Status.InternalServerError, JsNull))

      there were noCallsTo(twirpClient)
      result ==== Seq.empty
    }

    "returns empty set when server responds with an exception" in new RolloutDisabledContext {
      override def response = Future.exception(new Exception())

      there were noCallsTo(twirpClient)
      result ==== Seq.empty
    }
  }

  "rollout enabled" >> {
    "returns counts on successful response" in new RolloutEnabledContext {
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

    "returns empty set when server responds with an exception" in new RolloutEnabledContext {
      override def response = Future.exception(new Exception())

      there were noCallsTo(jsonClient)
      result ==== Seq.empty
    }
  }
}
