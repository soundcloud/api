package com.soundcloud.apipublic.integration

import com.soundcloud.jvmkit.module.util.config.DataSensitivity
import com.soundcloud.apipublic.testutilities.IntegrationTest
import com.twitter.finagle.http.{Request, RequestBuilder, Status}
import com.twitter.io.Buf
import com.twitter.util.Base64StringEncoder

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

    "return success for multipart request with oauth2 token and multiple values for the same key" in new IntegrationContext {
      val secret = config.get("CLIENT_SECRET", DataSensitivity.SENSITIVE)
      val request = RequestBuilder()
        .url(s"http://${server.serverAddress}/oauth2/token")
        .addHeader("X-Real-IP", "65.206.21.12")
        .addFormElement("grant_type" -> "client_credentials")
        .addFormElement("client_id" -> clientId)
        .addFormElement("client_id" -> clientId)
        .addFormElement("client_secret" -> secret)
        .addFormElement("playlist[tracks][][id]" -> "1")
        .addFormElement("playlist[tracks][][id]" -> "2")
        .addFormElement("playlist[tracks][][id]" -> "3")
        .buildFormPost(multipart = false)

      private val response: IntegrationTestHttpResponse = server.executeRequest(request)
      response.status ==== Status.Ok.code
    }

    "return success for Basic auth request with oauth2 token" in new IntegrationContext {
      val secret = config.get("CLIENT_SECRET", DataSensitivity.SENSITIVE)
      val encodedAuth = Base64StringEncoder.encode(new String(clientId + ":" + secret).getBytes)

      val request = RequestBuilder()
        .url(
          Request.queryString(s"http://${server.serverAddress}/oauth2/token", Map("grant_type" -> "client_credentials"))
        )
        .addHeader("X-Real-IP", "65.206.21.12")
        .addHeader("Authorization", s"Basic $encodedAuth")
        .buildPost(Buf.Empty)

      private val response: IntegrationTestHttpResponse = server.executeRequest(request)
      response.status ==== Status.Ok.code
    }

    "return forbidden for Muzooka when not signed properly" in new IntegrationContext {
      val request = RequestBuilder()
        .url(
          Request.queryString(s"http://${server.serverAddress}/muzooka/webhook")
        )
        .addHeader("X-Real-IP", "65.206.21.12")
        .addHeader("X-Signature", "sha1=WRONG_SIGNATURE")
        .buildPost(Buf.Utf8("Empty Body"))

      private val response: IntegrationTestHttpResponse = server.executeRequest(request)
      response.status ==== Status.Forbidden.code
    }
  }
}
