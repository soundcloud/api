package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.Handler
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.WebProfile
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Method
import com.twitter.util.Future
import play.api.libs.json.Json

class WebProfilesHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val mockMoshimoshiClient = mock[MoshimoshiClient]
    val session = anonymousSession
    val userAuthentication = new FakeUserAuthentication(session)
    val webProfilesHandler = new WebProfilesHandler(userAuthentication, mockMoshimoshiClient)
    val userUrn = Urn("soundcloud", "users", "1")

    override def routingDefinitions(): List[(Method, String, Handler)] =
      Routing.forWebProfilesHandler(webProfilesHandler)
  }

  "#getWebProfiles" >> {
    "on moshimoshi success" >> {
      trait SuccessContext extends Context {
        val webProfiles = Fixtures.webProfiles.as[List[WebProfile]]
        mockMoshimoshiClient.userWebProfiles(session, userUrn) returns Future.value(webProfiles)
      }

      "returns 200 with web profiles" in new SuccessContext {
        val response = get("/users/1/web-profiles")
        response.status.code ==== 200
        response.contentString ==== Json.stringify(Json.toJson(webProfiles))
      }
    }

    "on moshimoshi failure" >> {
      trait FailureContext extends Context {
        mockMoshimoshiClient.userWebProfiles(session, userUrn) throws new IllegalStateException
      }

      "throws an IllegalStateException" in new FailureContext {
        get("/users/1/web-profiles") must throwA[IllegalStateException]
      }
    }

    "on bad request" >> {
      "throws an IllegalArgumentException" in new Context {
        get("/users/()ytytyty-!\"\"\"/web-profiles") must throwA[IllegalArgumentException]
      }
    }
  }
}
