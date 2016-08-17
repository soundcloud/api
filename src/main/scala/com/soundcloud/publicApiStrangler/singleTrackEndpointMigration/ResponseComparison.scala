package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{HeaderMap, Request, Response, Status}
import com.twitter.util.{Return, Try}
import play.api.libs.json.JsObject

import scala.collection.JavaConversions._

class ResponseComparison(telemetry: Telemetry) {

  private val logger = SoundCloudLoggerFactory.getLogger("SingleTrackComparison")

  val comparisonMetric = telemetry.histogram(
    "single_track_endpoint_comparison",
    "distribution of number of different attributes that are served from strangler implementation",
    List(),
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

        val sanitizedMigrationResponseString = stringJsonpString(migrationRes.contentString)
        val migrationJsonTry = Try(Json.fromString(sanitizedMigrationResponseString).as[JsObject])

        (legacyJsonTry, migrationJsonTry) match {
          case (Return(legacyJson), Return(migrationJson)) => {

            val bodyDiffCount = calcBodyDiffCount(legacyJson, migrationJson)
            val headerDiffCount = calcHeaderDiffCount(originalRes.headerMap, migrationRes.headerMap)

            if (bodyDiffCount > 0) {
              reportBodyDifference
            }

            if (headerDiffCount > 0) {
              reportHeaderDIfference(originalRes, migrationRes)
            }

            comparisonMetric.labels().observe(bodyDiffCount)
          }

          case (legacyResult, migrationResult) =>
            reportJsonFailure(request, originalRes, migrationRes, legacyResult, migrationResult)
        }
      } else {
        reportUnexpectedMoshiStatus(request, originalRes, migrationRes)
      }
    } else {
      reportStatusDifference(request, originalRes, migrationRes)
    }
  }

  private def stringJsonpString(contentString: String): String = {
    val jsonpRegex = """^\/\*\*\/.*(\{.*\})\);$""".r

    contentString match {
      case jsonpRegex(json) => json
      case _ => contentString
    }
  }

  private def reportBodyDifference: Unit = {
    failuresCounter.labels("differentBodyAttributesCount").inc()
  }

  private def reportHeaderDIfference(originalRes: Response, migrationRes: Response): Unit = {
    logger.info("============tracks/:id endpoint header difference=========")
    logger.info(originalRes.headerMap.toString)
    logger.info(migrationRes.headerMap.toString)
    failuresCounter.labels("differentHeaderCount").inc()
  }

  def reportJsonFailure(request: Request, originalRes: Response, migrationRes: Response, legacyResult: Try[JsObject], migrationResult: Try[JsObject]): Unit = {
    logger.info("============tracks/:id endpoint json parse failure=========")
    logger.info(originalRes.contentString)
    logger.info(migrationRes.contentString)
    logger.info(legacyResult.toString)
    logger.info(migrationResult.toString)
    logger.info(request.toString)
    logger.info(request.headerMap.toString)
    failuresCounter.labels("jsonFailure").inc()
  }

  private def reportUnexpectedMoshiStatus(request: Request, originalRes: Response, migrationRes: Response): Unit = {
    logger.info("============tracks/:id endpoint unexpected moshi status=========")
    logger.info(request.toString)
    logger.info(request.headerMap.toString)
    logger.info(s"legacy res : ${originalRes.toString}")
    logger.info(s"migration res : ${migrationRes.toString}")
    failuresCounter.labels("unexpectedMoshiStatusCode").inc()
  }

  private def reportStatusDifference(request: Request, originalRes: Response, migrationRes: Response): Unit = {
    logger.info("============tracks/:id endpoint status difference=========")
    logger.info(request.toString)
    logger.info(request.headerMap.toString)
    logger.info(s"legacy res : ${originalRes.toString}")
    logger.info(s"migration res : ${migrationRes.toString}")
    failuresCounter.labels("differentStatusCodes").inc()
    statusCodeDifferenceCounter.labels(originalRes.statusCode.toString, migrationRes.statusCode.toString).inc()
  }

  private def calcBodyDiffCount(legacyJson: JsObject, migrationJson: JsObject): Int = {
    val extraAttributesCount = (migrationJson.fieldSet.map(_._1) diff legacyJson.fieldSet.map(_._1)).size

    val differentAttributeCount = legacyJson.fields.count {
      case (key, jsValue) =>
        jsValue != migrationJson \ key
    }

    differentAttributeCount + extraAttributesCount
  }

  private def calcHeaderDiffCount(legacyHeaders: HeaderMap, migrationHeaders: HeaderMap): Int = {
    val extraHeaders = (migrationHeaders.keys.toSeq diff legacyHeaders.keys.toSeq) diff ignoredHeaders

    val differentAttributeCount = legacyHeaders.count {
      case (key, value) =>
        !ignoredHeadersSet.contains(key) && value != migrationHeaders.get(key)
    }

    extraHeaders.size + differentAttributeCount
  }

  private val ignoredHeaders = Seq(
    "Access-Control-Allow-Headers",
    "Access-Control-Allow-Methods",
    "Access-Control-Allow-Origin",
    "Access-Control-Expose-Headers",
    "Cache-Control",
    "Content-Length",
    "Connection",
    "Date",
    "ETag",
    "Server",
    "Status",
    "Vary",
    "X-Powered-By",
    "X-Runtime"
  )

  private val ignoredHeadersSet = ignoredHeaders.toSet
}
