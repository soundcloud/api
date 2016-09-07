package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.http.{Request, Response, Status}
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

      val failuresCount = collectorRegistry.getSampleValue(
        "single_track_endpoint_failures",
        Array("type", "system"),
        Array("differentStatusCodes", "TEST-APP")
      )
      failuresCount ==== 1d

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

    "reports zero differences when responses are the same" in new Context {
      List(
        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}", "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"),
        ("{}", "{}"),
        ("{\"id\":987}", "{\"id\":987}")
      ).foreach {
        case (legacyResponseString, migrationResponseString) =>
          val legacyRes = generateResponse(legacyResponseString)
          val migrationRes = generateResponse(migrationResponseString)

          responseComparison.report(Request(), legacyRes, migrationRes)
          val value = collectorRegistry.getSampleValue(
            "single_track_endpoint_comparison_sum",
            Array("system"),
            Array("TEST-APP")
          )
          value ==== 0d
      }
    }

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
      val legacyRes = generateResponse("""{"kind": "track", "sounds_really_great": "true"}""")
      val migrationRes = generateResponse("""{"kind": "track", "sounds_really_great": "false"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("sounds_really_great", "TEST-APP")
      )
      value ==== 1d
    }

    "ignores ISRC when comparing attributes" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "isrc": "foo"}""")
      val migrationRes = generateResponse("""{"kind": "track", "isrc": "bar"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("isrc", "TEST-APP")
      )
      value must beNull
    }

    "treats HTTP/HTTPS as identical when comparing permalink_url attribute" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "permalink_url": "http://example.com/123"}""")
      val migrationRes = generateResponse("""{"kind": "track", "permalink_url": "https://example.com/123"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("permalink_url", "TEST-APP")
      )
      value must beNull
    }

    "records permalink_url differences" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "permalink_url": "http://example.com/456"}""")
      val migrationRes = generateResponse("""{"kind": "track", "permalink_url": "https://example.com/123"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("permalink_url", "TEST-APP")
      )
      value ==== 1d
    }

    "ignores download_url when only present in migrated response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track"}""")
      val migrationRes = generateResponse("""{"kind": "track", "download_url": "http://example.com/muzik.mp5"}""")

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
      val legacyRes = generateResponse("""{"kind": "track", "download_url": "http://example.com/happy_snail_song.mp3"}""")
      val migrationRes = generateResponse("""{"kind": "track", "download_url": "http://example.com/muzik.mp5"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("download_url", "TEST-APP")
      )
      value ==== 1d
    }

    "ignores stream_url when only present in migrated response" in new Context {
      val legacyRes = generateResponse("""{"kind": "track"}""")
      val migrationRes = generateResponse("""{"kind": "track", "stream_url": "http://example.com/muzik.mp5"}""")

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

    "records stream_url difference when present in both" in new Context {
      val legacyRes = generateResponse("""{"kind": "track", "stream_url": "http://example.com/happy_snail_song.mp3"}""")
      val migrationRes = generateResponse("""{"kind": "track", "stream_url": "http://example.com/muzik.mp5"}""")

      responseComparison.report(Request(), legacyRes, migrationRes)
      val value = collectorRegistry.getSampleValue(
        "attribute_value_different",
        Array("attribute", "system"),
        Array("stream_url", "TEST-APP")
      )
      value ==== 1d
    }

    "reports number of different attributes when responses are NOT the same" in new Context {
      List(
        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
          "{\"kind\":\"track2\",\"id\":988,\"user_id\":111}") ->(2, 1),

        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
          "{\"kind\":\"track\",\"id\":988,\"user_id\":111}") ->((2 + 1), 2),

        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
          "{\"kind\":\"track2\",\"id\":981,\"user_id\":112}") ->((2 + 1 + 3), 3)
      ).foreach {
        case ((legacyResponseString, migrationResponseString), (expectedCount, index)) =>
          val legacyRes = generateResponse(legacyResponseString)
          val migrationRes = generateResponse(migrationResponseString)

          responseComparison.report(Request(), legacyRes, migrationRes)
          val value = collectorRegistry.getSampleValue(
            "single_track_endpoint_comparison_sum",
            Array("system"),
            Array("TEST-APP")
          )
          value ==== expectedCount.toDouble

          val failuresCount = collectorRegistry.getSampleValue(
            "single_track_endpoint_failures",
            Array("type", "system"),
            Array("differentBodyAttributesCount", "TEST-APP")
          )
          failuresCount ==== index.toDouble
      }
    }

    "reports missing attributes as difference" in new Context {
      List(
        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
          "{\"kind\":\"track\",\"id\":987}") ->(1, 1),

        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
          "{\"kind\":\"track\"}") ->((1 + 2), 2),

        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
          "{}") ->((1 + 2 + 3), 3)
      ).foreach {
        case ((legacyResponseString, migrationResponseString), (expectedCount, index)) =>
          val legacyRes = generateResponse(legacyResponseString)
          val migrationRes = generateResponse(migrationResponseString)

          responseComparison.report(Request(), legacyRes, migrationRes)
          val value = collectorRegistry.getSampleValue(
            "single_track_endpoint_comparison_sum",
            Array("system"),
            Array("TEST-APP")
          )
          value ==== expectedCount.toDouble

          val failuresCount = collectorRegistry.getSampleValue(
            "single_track_endpoint_failures",
            Array("type", "system"),
            Array("differentBodyAttributesCount", "TEST-APP")
          )
          failuresCount ==== index.toDouble
      }
    }

    "reports extra attributes as difference" in new Context {
      List(
        ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
          "{\"kind\":\"track\",\"id\":987,\"user_id\":111,\"user_id_2\":111}") ->(1, 1),

        ("{\"kind\":\"track\"}",
          "{\"kind\":\"track\",\"id\":988,\"user_id\":111}") ->((1 + 2), 2)
      ).foreach {
        case ((legacyResponseString, migrationResponseString), (expectedCount, index)) =>
          val legacyRes = generateResponse(legacyResponseString)
          val migrationRes = generateResponse(migrationResponseString)

          responseComparison.report(Request(), legacyRes, migrationRes)
          val value = collectorRegistry.getSampleValue(
            "single_track_endpoint_comparison_sum",
            Array("system"),
            Array("TEST-APP")
          )
          value ==== expectedCount.toDouble

          val failuresCount = collectorRegistry.getSampleValue(
            "single_track_endpoint_failures",
            Array("type", "system"),
            Array("differentBodyAttributesCount", "TEST-APP")
          )
          failuresCount ==== index.toDouble
      }
    }

    "does not report ISRC differences" in new Context {
      List(
        ("{\"kind\":\"track\",\"id\":987,\"isrc\":\"D0NK3Y\"}",
          "{\"kind\":\"track\",\"id\":987,\"isrc\":\"D0NK3Y\"}"),

        ("{\"kind\":\"track\",\"id\":987,\"isrc\":\"D0NK3Y\"}",
          "{\"kind\":\"track\",\"id\":987,\"isrc\":\"DONK4Y\"}")
      ).foreach {
        case ((legacyResponseString, migrationResponseString)) =>
          val legacyRes = generateResponse(legacyResponseString)
          val migrationRes = generateResponse(migrationResponseString)

          responseComparison.report(Request(), legacyRes, migrationRes)
          val value = collectorRegistry.getSampleValue(
            "single_track_endpoint_comparison_sum",
            Array("system"),
            Array("TEST-APP")
          )
          value ==== 0.0d

          val failuresCount = collectorRegistry.getSampleValue(
            "single_track_endpoint_failures",
            Array("type", "system"),
            Array("differentBodyAttributesCount", "TEST-APP")
          )
          failuresCount ==== null
      }
    }

    "does not report count differences" in new Context {
      List(
        ("{\"playback_count\":1}", "{\"playback_count\":2}"),
        ("{\"download_count\":1}", "{\"download_count\":2}"),
        ("{\"favoritings_count\":1}", "{\"favoritings_count\":2}"),
        ("{\"comment_count\":1}", "{\"comment_count\":2}")
      ).foreach {
        case ((legacyResponseString, migrationResponseString)) =>
          val legacyRes = generateResponse(legacyResponseString)
          val migrationRes = generateResponse(migrationResponseString)

          responseComparison.report(Request(), legacyRes, migrationRes)
          val value = collectorRegistry.getSampleValue(
            "single_track_endpoint_comparison_sum",
            Array("system"),
            Array("TEST-APP")
          )
          value ==== 0.0d

          val failuresCount = collectorRegistry.getSampleValue(
            "single_track_endpoint_failures",
            Array("type", "system"),
            Array("differentBodyAttributesCount", "TEST-APP")
          )
          failuresCount ==== null
      }
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
      val jsonComparisonFailure = collectorRegistry.getSampleValue(
        "single_track_endpoint_comparison_sum",
        Array("system"),
        Array("TEST-APP")
      )
      jsonComparisonFailure ==== 0d
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

  "body checks" >> {

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
}
