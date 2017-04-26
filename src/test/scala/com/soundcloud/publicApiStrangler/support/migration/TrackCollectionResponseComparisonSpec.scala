package com.soundcloud.publicApiStrangler.support.migration

import com.soundcloud.jvmkit.module.telemetry.{MetricsRegistryImpl, Telemetry}
import com.soundcloud.jvmkit.module.util.config.InMemoryConfig
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.NonFatal
import io.prometheus.client.CollectorRegistry

class TrackCollectionResponseComparisonSpec extends UnitSpecification {

  trait Context extends Scope {
    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config.getApplicationName, new MetricsRegistryImpl(collectorRegistry))
    val responseComparison = new SingleTrackResponseComparison(telemetry)

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

  //  TODO:
  //  "body checks" >> {
  //    ...
  //  }

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
