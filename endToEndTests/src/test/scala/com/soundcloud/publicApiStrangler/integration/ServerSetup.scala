package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.testutilities.SpinningUpAppSupport
import com.twitter.finagle.http.{HeaderMap, Request}

trait ServerSetup extends IntegrationSpecification with SpinningUpAppSupport {

  trait IntegrationContext extends Context {
    val server = TestServer("publicapistrangler", 5000)

    def clientId = "u1aX7EnUd90ul1sbwLwj7cN6fqytmrcV" //config.get("CLIENT_ID", DataSensitivity.NON_SENSITIVE)
    def token = "1-292145-948745750-e77c043ccc985" //config.get("ACCESS_TOKEN", DataSensitivity.NON_SENSITIVE)

    lazy val freeTierTrackId = "405325995"
    lazy val freeTierNonStreamableTrackId = "1015448728"
    lazy val highTierTrackId = "653338388"
    lazy val blockedTrackId = "945370459"
    lazy val paywalledTrackId = "974675008"

    def path(path: String, params: Map[String, String] = Map.empty): String = {
      Request.queryString(path, Map("client_id" -> clientId) ++ params)
    }

    def authenticatedUSHeaders =
      HeaderMap("Authorization" -> s"OAuth $token", "X-Real-IP" -> "65.206.21.12")
  }
}
