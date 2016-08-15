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

  val failuresCounter = telemetry.counter(
    "single_track_endpoint_failures",
    "counter for response comparison failures",
    "type"
  )

  def report(request: Request, originalRes: Response, migrationRes: Response): Unit = {

    if (originalRes.status == migrationRes.status) {

      if (migrationRes.status == Status.Ok || migrationRes.status == Status.NotFound) {
        val legacyJsonTry = Try(Json.fromString(originalRes.contentString).as[JsObject])
        val migrationJsonTry = Try(Json.fromString(migrationRes.contentString).as[JsObject])

        (legacyJsonTry, migrationJsonTry) match {
          case (Return(legacyJson), Return(migrationJson)) => {
            val extraAttributesCount = (migrationJson.fieldSet.map(_._1) diff legacyJson.fieldSet.map(_._1)).size

            val differentAttributeCount = legacyJson.fields.count {
              case (key, jsValue) =>
                jsValue != migrationJson \ key
            }

            val differencesCount = differentAttributeCount + extraAttributesCount
            if (differencesCount > 0) {
              failuresCounter.labels("differentBodyAttributesCount").inc()
            }

            comparisonMetric.labels("success").observe(differentAttributeCount + extraAttributesCount)
          }

          case (legacyResult, migrationResult) =>
            logger.info("============tracks/:id endpoint json parse failure=========")
            logger.info(originalRes.contentString)
            logger.info(migrationRes.contentString)
            logger.info(legacyResult.toString)
            logger.info(migrationResult.toString)
            failuresCounter.labels("jsonFailure").inc()
        }
      } else {
        logger.info("============tracks/:id endpoint unexpected moshi status=========")
        logger.info(request.toString)
        logger.info(request.headerMap.toString)
        logger.info(s"legacy res : ${originalRes.toString}")
        logger.info(s"migration res : ${migrationRes.toString}")
        failuresCounter.labels("unexpectedMoshiStatusCode").inc()
      }
    } else {
      logger.info("============tracks/:id endpoint status difference1=========")
      logger.info(request.toString)
      logger.info(request.headerMap.toString)
      logger.info(s"legacy res : ${originalRes.toString}")
      logger.info(s"migration res : ${migrationRes.toString}")
      failuresCounter.labels("differentStatusCodes").inc()
      statusCodeDifferenceCounter.labels(originalRes.statusCode.toString, migrationRes.statusCode.toString).inc()
    }
  }

}
