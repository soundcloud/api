package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.publicApiStrangler.testutilities.IntegrationTest
import play.api.libs.json.{JsArray, JsObject}

class MothershipIntegrationSpec extends IntegrationTest {

  "/users/146532/comments" >> {
    "should parse response" in new IntegrationContext {
      val response = server.get("/users/146532/comments.json", authenticatedUSHeaders)

      response.status === 200

      val jsonResponse = response.json.as[JsArray]
      val last = jsonResponse.last.as[JsObject]
      (last \ "user" \ "id").as[Int] === 146532
    }
  }
}
