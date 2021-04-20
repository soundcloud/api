package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.publicApiStrangler.testutilities.IntegrationTest
import play.api.libs.json.JsObject

class MothershipIntegrationSpec extends IntegrationTest {

  "/me" >> {
    "should parse response" in new IntegrationContext {
      val response = server.get("/me.json", authenticatedUSHeaders)

      response.status === 200

      val jsonResponse = response.json.as[JsObject]
      jsonResponse.keys.size === 33
      (jsonResponse \ "id").as[Int] === 948745750
    }
  }
}
