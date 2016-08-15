package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Return, Try}
import play.api.libs.json.JsObject

import scala.collection.JavaConversions._

class ResponseComparison(telemetry: Telemetry) {

  private val logger = SoundCloudLoggerFactory.getLogger("SingleTrackComparison")

  val comparisonMetric = telemetry.histogram(
    "single_track_endpoint_comparison",
    "distribution of number of different attributes that are served from strangler implementation",
    List("status"),
    (0 to 60).toList.map(_.toDouble): _*
  )

  val statusCodeDifferenceCounter = telemetry.counter(
    "single_track_endpoint_status_code_difference",
    "counter for response comparison where status codes are different",
    "legacy", "migration"
  )

  def report(request: Request, originalRes: Response, migrationRes: Response): Unit = {

    if (originalRes.status == migrationRes.status) {

      if (migrationRes.status == Status.Ok || migrationRes.status == Status.NotFound) {
        val mainJsonTry = Try(Json.fromString(originalRes.contentString).as[JsObject])
        val migrationJsonTry = Try(Json.fromString(migrationRes.contentString).as[JsObject])

        (mainJsonTry, migrationJsonTry) match {
          case (Return(mainJson), Return(migrationJson)) => {
            val extraAttributesCount = (migrationJson.fieldSet.map(_._1) diff mainJson.fieldSet.map(_._1)).size

            val differentAttributeCount = mainJson.fields.count {
              case (key, jsValue) =>
                jsValue != migrationJson \ key
            }

            comparisonMetric.labels("success").observe(differentAttributeCount + extraAttributesCount)
          }

          case _ => comparisonMetric.labels("jsonFailure").observe(0)
        }
      } else {
        comparisonMetric.labels("unexpectedMoshiStatusCode").observe(0)
        logger.info("============tracks/:id endpoint unexpected moshi status=========")
        logger.info(request.toString)
        logger.info(request.headerMap.toString)
        logger.info(s"legacy res : ${originalRes.toString}")
        logger.info(s"migration res : ${migrationRes.toString}")
      }
    } else {
      comparisonMetric.labels("statusCodeFailure").observe(0)
      statusCodeDifferenceCounter.labels(originalRes.statusCode.toString, migrationRes.statusCode.toString).inc()

      logger.info("============tracks/:id endpoint status difference1=========")
      logger.info(request.toString)
      logger.info(request.headerMap.toString)
      logger.info(s"legacy res : ${originalRes.toString}")
      logger.info(s"migration res : ${migrationRes.toString}")
    }
  }

}
