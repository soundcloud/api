package com.soundcloud.publicApiStrangler.client.followcounts

import java.net.URLEncoder

import com.soundcloud.bff.services.JsonService
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsNull
import org.mockito.Mockito.when
import org.specs2.mutable.Before

class FollowCountsClientSpec extends UnitSpecification {

  trait Context extends Scope with Before {
    lazy val jsonService = mock[JsonService]
    lazy val user = Urn("soundcloud", "users", "1")

    val config = new InMemoryConfig
    config.set("STITCH_BULK_FETCH_MAX_ENTRIES", "10")

    lazy val client = new FollowCountsClient(jsonService, config)

    lazy val result = Await.result(client.counts(anonymousSession, Seq(user)))

    def response: Future[JsonResponse]

    override def before: Any = {
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

      when(jsonService.get(anonymousSession, Path() / "bulk", bulkParams, Params.empty)) thenReturn response
    }
  }

  "returns counts on successful response" in new Context {
    override def response = Future.value(JsonResponse(OkStatus, withContentsOf("stitch4follows", "bulk_follow_counts_response")))

    result ==== Seq(FollowCounts(user, 10, 20))
  }

  "returns empty set when server responds with a non OK status" in new Context {
    override def response = Future.value(JsonResponse(InternalServerErrorStatus, JsNull))

    result ==== Seq.empty
  }

  "returns empty set when server responds with an exception" in new Context {
    override def response = Future.exception(new Exception())

    result ==== Seq.empty
  }
}
