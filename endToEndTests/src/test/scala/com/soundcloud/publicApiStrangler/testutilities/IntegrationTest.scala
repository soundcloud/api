package com.soundcloud.publicApiStrangler.testutilities

import com.soundcloud.jvmkit.module.util.config.{AppConfig, DataSensitivity}
import com.twitter.finagle.http.{HeaderMap, Request}
import org.specs2.mutable.Specification

trait IntegrationTest extends Specification with SpinningUpAppSupport {
  sequential

  trait IntegrationContext extends Context {
    val server = TestServer("publicapistrangler", 5000)
    val config = new AppConfig

    def clientId = config.get("CLIENT_ID", DataSensitivity.SENSITIVE)

    def token = config.get("OAUTH_TOKEN", DataSensitivity.SENSITIVE)

    lazy val freeTierTrackId = "405325995"
    lazy val freeTierNonStreamableTrackId = "1015448728"
    lazy val highTierTrackId = "653338388"
    lazy val geoblockedInGermanyTrackId = "774186184"
    lazy val blockedTrackId = "974675008"
    lazy val rightsholderRestrictedTrackId = "945370459"

    def path(path: String, params: Map[String, String] = Map.empty): String = {
      Request.queryString(path, Map("client_id" -> clientId) ++ params)
    }

    def authenticatedUSHeaders =
      HeaderMap("Authorization" -> s"OAuth $token", "X-Real-IP" -> "65.206.21.12")
    def authenticatedDEHeaders =
      HeaderMap("Authorization" -> s"OAuth $token", "X-Real-IP" -> "2.16.7.255")
  }
}
