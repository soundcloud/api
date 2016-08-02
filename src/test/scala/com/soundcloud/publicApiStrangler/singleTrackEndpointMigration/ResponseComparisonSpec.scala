package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.scalakit.test.UnitSpecification
import io.prometheus.client.CollectorRegistry

class ResponseComparisonSpec extends UnitSpecification {

  trait Context extends Scope {
    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config, collectorRegistry)
    val responseComparison = new ResponseComparison(telemetry)
  }

  "reports zero differences when responses are the same" in new Context {
    List(
      ("{\"kind\":\"track\",\"id\":987,\"user_id\":111}", "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"),
      ("{}", "{}"),
      ("{\"id\":987}", "{\"id\":987}")
    ).foreach {
      case (originalResponse, migrationResponse) =>

        responseComparison.report(originalResponse, migrationResponse)
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
      case ((originalResponse, migrationResponse), expectedCount) =>

        responseComparison.report(originalResponse, migrationResponse)
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
      case ((originalResponse, migrationResponse), expectedCount) =>

        responseComparison.report(originalResponse, migrationResponse)
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
      case ((originalResponse, migrationResponse), expectedCount) =>

        responseComparison.report(originalResponse, migrationResponse)
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
      case ((originalResponse, migrationResponse), expectedCount) =>

        responseComparison.report(originalResponse, migrationResponse)
        val value = collectorRegistry.getSampleValue(
          "single_track_endpoint_comparison_count",
          Array("status", "system"),
          Array("failure", "TEST-APP")
        )
        value ==== expectedCount
    }
  }

}
