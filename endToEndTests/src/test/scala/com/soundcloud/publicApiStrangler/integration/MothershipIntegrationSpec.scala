package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.publicApiStrangler.testutilities.IntegrationTest
import play.api.libs.json.{JsArray, JsObject}

class MothershipIntegrationSpec extends IntegrationTest {

  "/users/19645907/web-profiles" >> {
    "should parse response" in new IntegrationContext {
      val response = server.get("/users/19645907/web-profiles.json", authenticatedUSHeaders)

      response.status === 200

      val jsonResponse = response.json.as[JsArray]
      val last = jsonResponse.last.as[JsObject]
      (last \ "id").as[Int] === 9222660
    }
  }
}
