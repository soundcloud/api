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

  "reports different response statuses" in new Context {
    val originalRes = Response(Status.NotFound)
    val migrationRes = Response(Status.Ok)

    responseComparison.report(Request(), originalRes, migrationRes)

    val histogramCount = collectorRegistry.getSampleValue(
      "single_track_endpoint_comparison_sum",
      Array("status", "system"),
      Array("statusCodeFailure", "TEST-APP")
    )
    histogramCount ==== 0

    val counterValue = collectorRegistry.getSampleValue(
      "single_track_endpoint_status_code_difference",
      Array("legacy", "migration", "system"),
      Array("404", "200", "TEST-APP")
    )
    counterValue ==== 1
  }

  "reports zero differences when responses are the same" in new Context {
    List(
      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}", "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"),
      ("{}", "{}"),
      ("{\"id\":987}", "{\"id\":987}")
    ).foreach {
      case (originalResponseString, migrationResponseString) =>
        val originalRes = generateResponse(originalResponseString)
        val migrationRes = generateResponse(migrationResponseString)

        responseComparison.report(Request(), originalRes, migrationRes)
        val value = collectorRegistry.getSampleValue(
          "single_track_endpoint_comparison_sum",
          Array("status", "system"),
          Array("success", "TEST-APP")
        )
        value ==== 0
    }
  }

  "reports number of different attributes when responses are NOT the same" in new Context {
    List(
      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
        "{\"kind\":\"track2\",\"id\":988,\"user_id\":111}") -> 2,

      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
        "{\"kind\":\"track\",\"id\":988,\"user_id\":111}") -> (2 + 1),

      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
        "{\"kind\":\"track2\",\"id\":981,\"user_id\":112}") -> (2 + 1 + 3)
    ).foreach {
      case ((originalResponseString, migrationResponseString), expectedCount) =>
        val originalRes = generateResponse(originalResponseString)
        val migrationRes = generateResponse(migrationResponseString)

        responseComparison.report(Request(), originalRes, migrationRes)
        val value = collectorRegistry.getSampleValue(
          "single_track_endpoint_comparison_sum",
          Array("status", "system"),
          Array("success", "TEST-APP")
        )
        value ==== expectedCount
    }
  }

  "reports missing attributes as difference" in new Context {
    List(
      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
        "{\"kind\":\"track\",\"id\":987}") -> 1,

      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
        "{\"kind\":\"track\"}") -> (1 + 2),

      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
        "{}") -> (1 + 2 + 3)
    ).foreach {
      case ((originalResponseString, migrationResponseString), expectedCount) =>
        val originalRes = generateResponse(originalResponseString)
        val migrationRes = generateResponse(migrationResponseString)

        responseComparison.report(Request(), originalRes, migrationRes)
        val value = collectorRegistry.getSampleValue(
          "single_track_endpoint_comparison_sum",
          Array("status", "system"),
          Array("success", "TEST-APP")
        )
        value ==== expectedCount
    }
  }

  "reports extra attributes as difference" in new Context {
    List(
      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}",
        "{\"kind\":\"track\",\"id\":987,\"user_id\":111,\"user_id_2\":111}") -> 1,

      ("{\"kind\":\"track\"}",
        "{\"kind\":\"track\",\"id\":988,\"user_id\":111}") -> (1 + 2)
    ).foreach {
      case ((originalResponseString, migrationResponseString), expectedCount) =>
        val originalRes = generateResponse(originalResponseString)
        val migrationRes = generateResponse(migrationResponseString)

        responseComparison.report(Request(), originalRes, migrationRes)
        val value = collectorRegistry.getSampleValue(
          "single_track_endpoint_comparison_sum",
          Array("status", "system"),
          Array("success", "TEST-APP")
        )
        value ==== expectedCount
    }
  }

  "reports any errors happening during comparison" in new Context {
    List(
      ("{\"kind\":}", // invalid json
        "{\"kind\":\"track\",\"id\":987,\"user_id\":111,\"user_id_2\":111}") -> 1,

      ("{\"kind\":\"track\"}",
        "{\"kind\":\"track\",\"id\"") -> 2 // invalid json
    ).foreach {
      case ((originalResponseString, migrationResponseString), expectedCount) =>
        val originalRes = generateResponse(originalResponseString)
        val migrationRes = generateResponse(migrationResponseString)

        responseComparison.report(Request(), originalRes, migrationRes)
        val value = collectorRegistry.getSampleValue(
          "single_track_endpoint_comparison_count",
          Array("status", "system"),
          Array("jsonFailure", "TEST-APP")
        )
        value ==== expectedCount
    }
  }

}
