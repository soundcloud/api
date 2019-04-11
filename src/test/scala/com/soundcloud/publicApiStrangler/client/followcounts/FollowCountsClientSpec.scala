package com.soundcloud.publicApiStrangler.client.followcounts

import java.net.URLEncoder

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.config.InMemoryConfig
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.withContentsOf
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before
import play.api.libs.json.JsNull

class FollowCountsClientSpec extends UnitSpecification {

  trait Context extends Scope with Before {
    lazy val jsonService = mock[JsonClient]
    lazy val user = Urn("soundcloud", "users", "1")

    val config = new InMemoryConfig
    config.set("STITCH_BULK_FETCH_MAX_ENTRIES", "10")

    lazy val client = new FollowCountsClient(jsonService, config)

    lazy val result = Await.result(client.counts(anonymousSession, Seq(user)))

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

      when(jsonService.getWithSession(anonymousSession, Path() / "bulk", bulkParams, Headers.empty)) thenReturn response
    }
  }

  "returns counts on successful response" in new Context {
    override def response = Future.value(jsonResponse(Status.Ok, withContentsOf("stitch4follows", "bulk_follow_counts_response")))

    result ==== Seq(FollowCounts(user, 10, 20))
  }

  "returns empty set when server responds with a non OK status" in new Context {
    override def response = Future.value(jsonResponse(Status.InternalServerError, JsNull))

    result ==== Seq.empty
  }

  "returns empty set when server responds with an exception" in new Context {
    override def response = Future.exception(new Exception())

    result ==== Seq.empty
  }
}
