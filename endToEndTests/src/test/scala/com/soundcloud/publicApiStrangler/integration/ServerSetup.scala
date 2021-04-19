package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.jvmkit.module.util.config.{AppConfig, DataSensitivity}
import com.soundcloud.testutilities.SpinningUpAppSupport
import com.twitter.finagle.http.{HeaderMap, Request}

trait ServerSetup extends IntegrationSpecification with SpinningUpAppSupport {

  trait IntegrationContext extends Context {
    val server = TestServer("publicapistrangler", 5000)
    val config = new AppConfig

    def clientId = config.get("CLIENT_ID", DataSensitivity.NON_SENSITIVE)
    def token = config.get("ACCESS_TOKEN", DataSensitivity.NON_SENSITIVE)

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
