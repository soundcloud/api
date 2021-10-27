package com.soundcloud.publicApiStrangler.integration

import com.soundcloud.publicApiStrangler.testutilities.IntegrationTest
import play.api.libs.json.JsArray

class SearchIntegrationSpec extends IntegrationTest {

  trait SearchContext extends IntegrationContext {

    def searchPath(route: String = "/tracks", params: Map[String, String] = Map.empty): String = {
      super.path(route, Map("linked_partitioning" -> "true", "limit" -> "5") ++ params)
    }
  }

  "/tracks" >> {
    "should return only full tracks, if no access are present (free tier is default)" in new SearchContext {
      val response = server.get(searchPath(params = Map("q" -> "Crazy In Love")), authenticatedDEHeaders)

      response.status === 200

      val searchResult = (response.json \ "collection").as[JsArray].value
      searchResult.map(item => (item \ "access").as[String] mustEqual "playable")
    }

    "should return full tracks and snippets, if access=playable,preview" in new SearchContext {
      val response = server.get(
        searchPath(params = Map("q" -> "better", "limit" -> "30", "access" -> "playable,preview")),
        authenticatedDEHeaders
      )

      response.status === 200

      val searchResult = (response.json \ "collection").as[JsArray].value

      searchResult.map(item => (item \ "access").as[String] must beOneOf("playable", "preview"))
      searchResult.map(item => (item \ "access").as[String]) must contain(
        allOf("playable", "preview")
      )
    }

    "should return blocked tracks as well, full access" in new SearchContext {
      val response = server.get(
        searchPath(params = Map("q" -> "better", "limit" -> "30", "access" -> "playable,preview,blocked")),
        authenticatedDEHeaders
      )

      response.status === 200

      val searchResult = (response.json \ "collection").as[JsArray].value
      searchResult.map(item => (item \ "access").as[String]) must contain("blocked")
    }

    "should return geoblocked tracks as well, full access" in new SearchContext {
      val response = server.get(
        searchPath(params = Map(
          "q" -> "Test - Geo blocked track",
          "ids" -> geoblockedInGermanyTrackId,
          "access" -> "playable,preview,blocked"
        )
        ),
        authenticatedDEHeaders
      )

      response.status === 200

      val searchResult = (response.json \ "collection").as[JsArray].value
      searchResult.map(item => (item \ "access").as[String]) must contain("blocked")
      searchResult.exists(item => {
        val maybeCountryCodes = (item \ "available_country_codes").asOpt[List[String]]
        maybeCountryCodes.exists(!_.contains("DE"))
      }) must beTrue
    }
  }
}
