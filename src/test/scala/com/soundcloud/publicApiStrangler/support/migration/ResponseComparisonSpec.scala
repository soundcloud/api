package com.soundcloud.publicApiStrangler.support.migration

import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.NonFatal
import io.prometheus.client.CollectorRegistry

class ResponseComparisonSpec extends UnitSpecification {
  trait Context extends Scope {
    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config, collectorRegistry)
    val responseComparison = new ResponseComparison(telemetry)

    def generateResponse(contentString: String) = {
      val res = Response()
      res.setContentString(contentString)
      res
    }
  }

  "status checks" >> {

    "reports different response statuses" in new Context {
      val legacyRes = Response(Status.NotFound)
      val migrationRes = Response(Status.Ok)

      responseComparison.report(Request(), legacyRes, migrationRes)

      val counterValue = collectorRegistry.getSampleValue(
        "single_track_endpoint_status_code_difference",
        Array("legacy", "migration", "system"),
        Array("404", "200", "TEST-APP")
      )
      counterValue ==== 1d
    }

    "reports unexpected moshi statuses" in new Context {
      val legacyRes = Response(Status.Gone)
      val migrationRes = Response(Status.Gone)

      responseComparison.report(Request(), legacyRes, migrationRes)

      val failuresCount = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("unexpectedMoshiStatusCode", "TEST-APP")
      )
      failuresCount ==== 1d
    }
  }

  "body checks" >> {
    "reports attributes that are only present in the legacy response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 100}""")
      val migrationRes = generateResponse("""{"kind": "track"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "id", "TEST-APP")
      )
      value ==== 1d
    }

    "reports attributes that are only present in the migration response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 100}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 100, "user_id": 7110}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("migration", "user_id", "TEST-APP")
      )
      value ==== 1d
    }

    "reports attributes that have different values" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "sounds_really_great": "true"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "sounds_really_great": "false"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("sounds_really_great", "TEST-APP")
      )
      value ==== 1d
    }

    "ignores ISRC when comparing attributes" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "isrc": "foo"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "isrc": "bar"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("isrc", "TEST-APP")
      )
      value must beNull
    }

    "does not ignore whitespace pruning in genre comparison" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "genre": "   Foo    Bar   "}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "genre": "Foo Bar"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("genre", "TEST-APP")
      )
      value mustEqual 1.0
    }

    "interprets null in genre for migration response as equivalent to empty string in legacy " in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "genre": null}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "genre": ""}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("genre", "TEST-APP")
      )
      value must beNull
    }

    "ignores target attribute in description anchor tags" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "description": "<a href=\"https://theartistunion.com/tracks/9a2f32\" rel=\"nofollow\">"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "description": "<a href=\"https://theartistunion.com/tracks/9a2f32\" rel=\"nofollow\" target=\"_blank\">"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("description", "TEST-APP")
      )
      value must beNull
    }

    "treats HTTP/HTTPS as identical when comparing permalink_url attribute" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "permalink_url": "http://example.com/123"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "permalink_url": "https://example.com/123"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("permalink_url", "TEST-APP")
      )
      value must beNull
    }

    "ignores /preferFlash=false&useHTML5Audio=true in permalink_url" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "permalink_url": "https://example.com/123/preferFlash=false&useHTML5Audio=true"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "permalink_url": "https://example.com/123"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("permalink_url", "TEST-APP")
      )
      value must beNull
    }

    "ignores /preferFlash=false&useHTML5Audio=true in uri" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "uri": "https://example.com/123/preferFlash=false&useHTML5Audio=true"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "uri": "https://example.com/123"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("uri", "TEST-APP")
      )
      value must beNull
    }

    "ignores /preferFlash=false&useHTML5Audio=true in stream_url" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "stream_url": "https://example.com/123/preferFlash=false&useHTML5Audio=true"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "stream_url": "https://example.com/123"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("stream_url", "TEST-APP")
      )
      value must beNull
    }

    "records permalink_url differences" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "permalink_url": "http://example.com/456"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "permalink_url": "https://example.com/123"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("permalink_url", "TEST-APP")
      )
      value ==== 1d
    }

    "ignores download_url when only present in migrated response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "download_url": "http://example.com/muzik.mp5"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("download_url", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("migration", "download_url", "TEST-APP")
      ) must beNull
    }

    "records download_url difference when present in both" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "download_url": "http://example.com/happy_snail_song.mp3"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "download_url": "http://example.com/muzik.mp5"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("download_url", "TEST-APP")
      )
      value ==== 1d
    }

    "ignores stream_url when only present in migrated response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "stream_url": "http://example.com/muzik.mp5"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("stream_url", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("migration", "stream_url", "TEST-APP")
      ) must beNull
    }

    "ignores reposts_count in legacy response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "reposts_count": 123}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("reposts_count", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "reposts_count", "TEST-APP")
      ) must beNull
    }

    "ignores likes_count in legacy response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "likes_count": 123}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("reposts_count", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "likes_count", "TEST-APP")
      ) must beNull
    }

    "records stream_url difference when present in both" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "stream_url": "http://example.com/happy_snail_song.mp3"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "stream_url": "http://example.com/muzik.mp5"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("stream_url", "TEST-APP")
      )
      value ==== 1d
    }

    "does not record tag_list difference if tag list is the same (simple case)" in new Context {
      val legacyRes    = generateResponse("""{"kind": "track", "id": 4, "tag_list": "abc"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "tag_list": "abc"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("tag_list", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "tag_list", "TEST-APP")
      ) must beNull
    }

    "does not record tag_list difference if tag list is the same" in new Context {
      val legacyRes    = generateResponse("""{"kind": "track", "id": 4, "tag_list": "m a y n o:oo=ooo b:bb=bbb z:zz=\"z z z\" p:pp=ppp"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "tag_list": "m a y n o:oo=ooo b:bb=bbb z:zz=\"z z z\" p:pp=ppp"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("tag_list", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "tag_list", "TEST-APP")
      ) must beNull
    }

    "does not record tag_list difference if sorting is different" in new Context {
      val legacyRes    = generateResponse("""{"kind": "track", "id": 4, "tag_list": "m a y n o:oo=ooo b:bb=bbb z:zz=\"z z z\" p:pp=ppp"}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "tag_list": "a m n y o:oo=ooo b:bb=bbb z:zz=\"z z z\" p:pp=ppp"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("tag_list", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "tag_list", "TEST-APP")
      ) must beNull
    }

    "does not record comment_count difference" in new Context {
      val legacyRes    = generateResponse("""{"kind": "track", "id": 4, "comment_count": 123}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "comment_count": 456}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("comment_count", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "comment_count", "TEST-APP")
      ) must beNull
    }

    "does not record favoritings_count difference" in new Context {
      val legacyRes    = generateResponse("""{"kind": "track", "id": 4, "favoritings_count": 123}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "favoritings_count": 456}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("favoritings_count", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "favoritings_count", "TEST-APP")
      ) must beNull
    }

    "does not record playback_count difference" in new Context {
      val legacyRes    = generateResponse("""{"kind": "track", "id": 4, "playback_count": 123}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "playback_count": 456}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("playback_count", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "playback_count", "TEST-APP")
      ) must beNull
    }

    "does not record download_count difference" in new Context {
      val legacyRes    = generateResponse("""{"kind": "track", "id": 4, "download_count": 123}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "download_count": 456}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("download_count", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "download_count", "TEST-APP")
      ) must beNull
    }

    "does not record downloadable difference" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "downloadable": true}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "downloadable": false}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("downloadable", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "downloadable", "TEST-APP")
      ) must beNull
    }

    "records difference in available_country_codes" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "available_country_codes": ["BE"]}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "available_country_codes": ["FR"]}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("available_country_codes", "TEST-APP")
      )

      value ==== 1d
    }

    "ignores difference for non-existant country codes in available_country_codes" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "id": 4, "available_country_codes": ["BE","XX"]}""")
      val migrationRes = generateResponse("""{"kind": "track", "id": 4, "available_country_codes": ["BE"]}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("available_country_codes", "TEST-APP")
      ) must beNull
      collectorRegistry.getSampleValue(
        "attribute_only_present_in",
        Array("response", "attribute", "system"),
        Array("legacy", "available_country_codes", "TEST-APP")
      ) must beNull
    }

    "reports any errors happening during comparison" in new Context {
      List(
        ("{\"kind\":}", // invalid json
          "{\"kind\":\"track\",\"id\":987,\"user_id\":111,\"user_id_2\":111}") -> 1,

        ("{\"kind\":\"track\"}",
          "{\"kind\":\"track\",\"id\"") -> 2 // invalid json
      ).foreach {
        case ((legacyResponseString, migrationResponseString), expectedCount) =>
          val legacyRes = generateResponse(legacyResponseString)
          val migrationRes = generateResponse(migrationResponseString)

          responseComparison.report(Request(), legacyRes, migrationRes)
          val value = collectorRegistry.getSampleValue(
            "single_track_endpoint_failures",
            Array("type", "system"),
            Array("jsonFailure", "TEST-APP")
          )
          value ==== expectedCount.toDouble
      }
    }
  }

  "jsonp checks" >> {
    "reports zero differences when identical json is wrapped in identical jsonp text in both responses" in new Context {
      val legacyRes = generateResponse("/**/__jp11({\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}});")
      val migrationRes = generateResponse("/**/__jp11({\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}});")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val jsonpTextFailure = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("jsonpTextFailure", "TEST-APP")
      )
      jsonpTextFailure must beNull
    }

    "reports comparison failure err when jsonp text is different" in new Context {
      val legacyRes = generateResponse("/**/__jp11({\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}});")
      val migrationRes = generateResponse("/**/__jp2({\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}});")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("jsonpTextFailure", "TEST-APP")
      )
      value ==== 1d
    }

    "reports comparison failure err when only legacy response is jsonp" in new Context {
      val legacyRes = generateResponse("/**/__jp11({\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}});")
      val migrationRes = generateResponse("{\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}}")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("jsonpTextFailure", "TEST-APP")
      )
      value ==== 1d
    }

    "reports comparison failure err when only migration response is jsonp" in new Context {
      val legacyRes = generateResponse("{\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}}")
      val migrationRes = generateResponse("/**/__jp11({\"kind\":\"track\",\"id\":270851500,\"user\":{\"id\":2541840,\"kind\":\"user\"}});")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("jsonpTextFailure", "TEST-APP")
      )
      value ==== 1d
    }
  }

  "response header checks" >> {

    "Does not report failures when headers are same" in new Context {
      val legacyRes = generateResponse("{}")
      legacyRes.headerMap.add("Content-Type", "application/json; charset=utf-8")
      val migrationRes = generateResponse("{}")
      migrationRes.headerMap.add("Content-Type", "application/json; charset=utf-8")

      responseComparison.report(Request(), legacyRes, migrationRes)

      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("differentHeaderCount", "TEST-APP")
      )
      value must beNull
    }

    "reports number of different headers when original has more" in new Context {
      val legacyRes = generateResponse("{}")
      legacyRes.headerMap.add("X-MovieName", "se7en")
      val migrationRes = generateResponse("{}")

      responseComparison.report(Request(), legacyRes, migrationRes)

      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("differentHeaderCount", "TEST-APP")
      )
      value ==== 1.toDouble
    }

    "reports number of different headers when original has fewer" in new Context {
      val legacyRes = generateResponse("{}")
      val migrationRes = generateResponse("{}")
      migrationRes.headerMap.add("X-MovieName", "se7en")

      responseComparison.report(Request(), legacyRes, migrationRes)

      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("differentHeaderCount", "TEST-APP")
      )
      value ==== 1.toDouble
    }

    "reports number of different headers when values for same header name differ" in new Context {
      val legacyRes = generateResponse("{}")
      legacyRes.headerMap.add("X-MovieName", "8ight")
      val migrationRes = generateResponse("{}")
      migrationRes.headerMap.add("X-MovieName", "se7en")

      responseComparison.report(Request(), legacyRes, migrationRes)

      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("differentHeaderCount", "TEST-APP")
      )
      value ==== 1.toDouble
    }

    "does not report difference for ignored headers" in new Context {
      val originalHeaderValue = "donkey"
      val migrationHeaderValue = "giraffe"

      val legacyRes = generateResponse("{}")
      legacyRes.headerMap.add("Access-Control-Allow-Headers", originalHeaderValue)
      legacyRes.headerMap.add("Access-Control-Allow-Methods", originalHeaderValue)
      legacyRes.headerMap.add("Access-Control-Allow-Origin", originalHeaderValue)
      legacyRes.headerMap.add("Access-Control-Expose-Headers", originalHeaderValue)
      legacyRes.headerMap.add("Cache-Control", originalHeaderValue)
      legacyRes.headerMap.add("Connection", originalHeaderValue)
      legacyRes.headerMap.add("Content-Length", originalHeaderValue)
      legacyRes.headerMap.add("Date", originalHeaderValue)
      legacyRes.headerMap.add("ETag", originalHeaderValue)
      legacyRes.headerMap.add("Server", originalHeaderValue)
      legacyRes.headerMap.add("Status", originalHeaderValue)
      legacyRes.headerMap.add("Vary", originalHeaderValue)
      legacyRes.headerMap.add("X-Powered-By", originalHeaderValue)
      legacyRes.headerMap.add("X-Runtime", originalHeaderValue)

      val migrationRes = generateResponse("{}")
      migrationRes.headerMap.add("Access-Control-Allow-Headers", migrationHeaderValue)
      migrationRes.headerMap.add("Access-Control-Allow-Methods", migrationHeaderValue)
      migrationRes.headerMap.add("Access-Control-Allow-Origin", migrationHeaderValue)
      migrationRes.headerMap.add("Access-Control-Expose-Headers", migrationHeaderValue)
      migrationRes.headerMap.add("Cache-Control", migrationHeaderValue)
      migrationRes.headerMap.add("Connection", migrationHeaderValue)
      migrationRes.headerMap.add("Content-Length", migrationHeaderValue)
      migrationRes.headerMap.add("Date", migrationHeaderValue)
      migrationRes.headerMap.add("ETag", migrationHeaderValue)
      migrationRes.headerMap.add("Server", migrationHeaderValue)
      migrationRes.headerMap.add("Status", migrationHeaderValue)
      migrationRes.headerMap.add("Vary", migrationHeaderValue)
      migrationRes.headerMap.add("X-Powered-By", migrationHeaderValue)
      migrationRes.headerMap.add("X-Runtime", migrationHeaderValue)

      responseComparison.report(Request(), legacyRes, migrationRes)

      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("differentHeaderCount", "TEST-APP")
      )
      value must beNull
    }

    "reports number of different headers when lots of headers are different" in new Context {
      val legacyRes = generateResponse("{}")
      legacyRes.headerMap.add("X-Animal", "giraffe")
      legacyRes.headerMap.add("X-MovieName", "8ight")
      legacyRes.headerMap.add("X-FavoriteColor", "orange")
      val migrationRes = generateResponse("{}")
      migrationRes.headerMap.add("X-Animal", "giraffe")
      migrationRes.headerMap.add("X-MovieName", "se7en")
      migrationRes.headerMap.add("X-Awesomeness", "9000")

      responseComparison.report(Request(), legacyRes, migrationRes)

      val value = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("differentHeaderCount", "TEST-APP")
      )
      value ==== 1d
    }
  }

  "handling failure" >> {
    "does not allow exceptions to propagate" in new Context {

      val legacy = mock[Response]
      val migration = mock[Response]

      legacy.status throws new RuntimeException("Oh noes")
      migration.status throws new RuntimeException("We're all doomed")

      try {
        responseComparison.report(Request(), legacy, migration)
      } catch {
        case NonFatal(_) => ko("Shouldn't have thrown an exception")
      }
    }
  }
}
