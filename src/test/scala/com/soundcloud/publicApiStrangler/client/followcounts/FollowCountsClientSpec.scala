package com.soundcloud.publicApiStrangler.client.followcounts

import java.net.URLEncoder

import com.soundcloud.bff.services.JsonService
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.config.{Config, DataSensitivity}
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.{Await, Future}

class FollowCountsClientSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val user = Urn("soundcloud", "users", "1")

    val jsonService = mock[JsonService]
    val config = mock[Config]

    lazy val client = new FollowCountsClient(jsonService, config)

    override def before = {
      val bulkParams = Params(
        "followingCounts" -> URLEncoder.encode(
          s"/timeseries?resolution=alltime&category=f.u&minus-category=n.f.u&keys=${user.getIdentifier}",
          "UTF-8"
        ),
        "followerCounts" -> URLEncoder.encode(
          s"/timeseries?resolution=alltime&category=f.b.u&minus-category=n.f.b.u&keys=${user.getIdentifier}",
          "UTF-8"
        )
      )
      when(jsonService.get(anonymousSession, Path() / "bulk", bulkParams, Params.empty)) thenReturn
        Future.value(JsonResponse(OkStatus, withContentsOf("stitch4counts", "bulk_follow_counts_response")))

      when(config.get("STITCH_BULK_FETCH_MAX_ENTRIES", DataSensitivity.NON_SENSITIVE)).thenReturn("10")
    }
  }

  "counts" in new Context {
    val result = Await.result(client.counts(anonymousSession, Seq(user)))

    result ==== Seq(FollowCounts(user, 10, 20))
  }
}
