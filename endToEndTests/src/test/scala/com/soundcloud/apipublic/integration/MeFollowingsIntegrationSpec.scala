package com.soundcloud.apipublic.integration

import com.soundcloud.apipublic.testutilities.IntegrationTest
import play.api.libs.json.JsValue

class MeFollowingsIntegrationSpec extends IntegrationTest {

  trait FollowingsContext extends IntegrationContext {
    def followingsPath(params: Map[String, String] = Map.empty): String =
      path("/me/followings", params)
  }

  "GET /me/followings" >> {
    "should return a non-empty collection of well-formed users when authenticated" in new FollowingsContext {
      val response = server.get(followingsPath(), authenticatedUSHeaders)
      response.status === 200

      val collection = (response.json \ "collection").as[List[JsValue]]
      collection must not(beEmpty)

      val firstUser = collection.head
      (firstUser \ "kind").as[String] === "user"
      (firstUser \ "id").asOpt[Long] must beSome
      (firstUser \ "urn").as[String] must startWith("soundcloud:users:")
      (firstUser \ "permalink_url").asOpt[String] must beSome
    }

    "should honour the limit parameter" in new FollowingsContext {
      val response = server.get(followingsPath(Map("limit" -> "1")), authenticatedUSHeaders)
      response.status === 200
      (response.json \ "collection").as[List[JsValue]].size === 1
    }

    "should return 401 without authentication" in new FollowingsContext {
      val response = server.get(followingsPath())
      response.status === 401
    }
  }
}
