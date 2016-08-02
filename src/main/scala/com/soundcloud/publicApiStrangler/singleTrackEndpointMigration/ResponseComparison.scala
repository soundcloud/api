package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.json.Json
import com.twitter.util.{Return, Try}
import play.api.libs.json.JsObject

import scala.collection.JavaConversions._

class ResponseComparison(telemetry: Telemetry) {

  val comparisonMetric = telemetry.histogram(
    "single_track_endpoint_comparison",
    "distribution of number of different attributes that are served from strangler implementation",
    List("status"),
    5, 10, 15, 20, 25, 30, 35, 40, 45
  )

  def report(originalResponseBody: String, migrationResponseBody: String): Unit = {

    val mainJsonTry = Try(Json.fromString(originalResponseBody).as[JsObject])
    val migrationJsonTry = Try(Json.fromString(migrationResponseBody).as[JsObject])

    (mainJsonTry, migrationJsonTry) match {
      case (Return(mainJson), Return(migrationJson)) => {
        val extraAttributesCount = (migrationJson.fieldSet.map(_._1) diff mainJson.fieldSet.map(_._1)).size

        val differentAttributeCount = mainJson.fields.count {
          case (key, jsValue) =>
            jsValue != migrationJson \ key
        }

        comparisonMetric.labels("success").observe(differentAttributeCount + extraAttributesCount)
      }

      case _ => comparisonMetric.labels("failure").observe(0)
    }
  }
}
