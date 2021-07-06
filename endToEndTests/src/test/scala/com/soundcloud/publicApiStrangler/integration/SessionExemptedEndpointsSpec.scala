package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.jvmkit.module.util.config.DataSensitivity
import com.soundcloud.publicApiStrangler.testutilities.IntegrationTest
import com.twitter.finagle.http.{RequestBuilder, Status}

class SessionExemptedEndpointsSpec extends IntegrationTest {

  "Public API Strangler" should {
    "return success when probing crossdomain filters endpoint" in new IntegrationContext {
      server.get("/crossdomain.xml").status ==== Status.Ok.code
      server.get("/robots.txt").status ==== Status.Ok.code
    }

    "return success for multipart request with oauth2 token" in new IntegrationContext {
      val secret = config.get("CLIENT_SECRET", DataSensitivity.SENSITIVE)
      val request = RequestBuilder()
        .url(s"http://${server.serverAddress}/oauth2/token")
        .addHeader("X-Real-IP", "65.206.21.12")
        .addFormElement("grant_type" -> "client_credentials")
        .addFormElement("client_id" -> clientId)
        .addFormElement("client_secret" -> secret)
        .buildFormPost(multipart = false)

      private val response: IntegrationTestHttpResponse = server.executeRequest(request)
      response.status ==== Status.Ok.code
    }
  }
}
