package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.publicApiStrangler.testutilities.IntegrationTest
import com.twitter.finagle.http.{Request, RequestBuilder, Status}
import play.api.libs.json.JsArray

class RepostClientIntegrationSpec extends IntegrationTest {
  trait SearchContext extends IntegrationContext
  "/reposters" >> {
    "should return successful response" in new IntegrationContext {
      val request = RequestBuilder()
        .url(
          Request.queryString(s"http://${server.serverAddress}/tracks/206491284/reposters")
        ).addHeader("Authorization", s"OAuth $token").buildGet
      private val response: IntegrationTestHttpResponse = server.executeRequest(request)
      response.status ==== Status.Ok.code
    }

    "should return successful repost counts" in new IntegrationContext {
      val request = RequestBuilder()
        .url(
          Request.queryString(s"http://${server.serverAddress}/tracks/206491284/reposters")
        ).addHeader("Authorization", s"OAuth $token").buildGet
      private val response: IntegrationTestHttpResponse = server.executeRequest(request)
      val searchResult = (response.json \ "collection").as[JsArray].value
      val repostCounts = searchResult.map(item => (item \ "reposts_count").as[Int])
      repostCounts.map(item => item must be_>(0))
    }
  }
}
