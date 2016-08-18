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

  def report(request: Request, legacyRes: Response, migrationRes: Response): Unit = {

    if (legacyRes.status == migrationRes.status) {

      if (migrationRes.status == Status.Ok || migrationRes.status == Status.NotFound) {
        val sanitizedLegacyResponseString = stringJsonpString(legacyRes.contentString)
        val legacyJsonTry = Try(Json.fromString(sanitizedLegacyResponseString).as[JsObject])

        val migrationJsonTry = Try(Json.fromString(migrationRes.contentString).as[JsObject])

        (legacyJsonTry, migrationJsonTry) match {
          case (Return(legacyJson), Return(migrationJson)) => {

            val bodyDiffCount = calcBodyDiffCount(legacyJson, migrationJson)
            if (bodyDiffCount > 0) {
              reportBodyDifference
            }

            val differentHeaders = getDifferentHeaders(legacyRes.headerMap, migrationRes.headerMap)
            if (differentHeaders.size > 0) {
              reportHeaderDifference(legacyRes, migrationRes, differentHeaders)
            }

            comparisonMetric.labels().observe(bodyDiffCount)
          }

          case (legacyResult, migrationResult) =>
            reportJsonFailure(request, legacyRes, migrationRes, legacyResult, migrationResult)
        }
      } else {
        reportUnexpectedMoshiStatus(request, legacyRes, migrationRes)
      }
    } else {
      reportStatusDifference(request, legacyRes, migrationRes)
    }
  }

  private def stringJsonpString(contentString: String): String = {
    val jsonpRegex = """\/\*\*\/.*\((\{.*\})\);""".r

    contentString match {
      case jsonpRegex(json) => json
      case _ => contentString
    }
  }

  private def reportBodyDifference: Unit = {
    failuresCounter.labels("differentBodyAttributesCount").inc()
  }

  private def reportHeaderDifference(legacyRes: Response, migrationRes: Response, differentHeaders: Seq[String]): Unit = {
    logger.info("============tracks/:id endpoint header difference=========")
    logger.info(legacyRes.headerMap.toString)
    logger.info(migrationRes.headerMap.toString)
    logger.info(s"Different headers : $differentHeaders")

    failuresCounter.labels("differentHeaderCount").inc()
  }

  def reportJsonFailure(request: Request, legacyRes: Response, migrationRes: Response, legacyResult: Try[JsObject], migrationResult: Try[JsObject]): Unit = {
    logger.info("============tracks/:id endpoint json parse failure=========")
    logger.info(legacyRes.contentString)
    logger.info(migrationRes.contentString)
    logger.info(legacyResult.toString)
    logger.info(migrationResult.toString)
    logger.info(request.toString)
    logger.info(request.headerMap.toString)
    failuresCounter.labels("jsonFailure").inc()
  }

  private def reportUnexpectedMoshiStatus(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    logger.info("============tracks/:id endpoint unexpected moshi status=========")
    logger.info(request.toString)
    logger.info(request.headerMap.toString)
    logger.info(s"legacy res : ${legacyRes.toString}")
    logger.info(s"migration res : ${migrationRes.toString}")
    failuresCounter.labels("unexpectedMoshiStatusCode").inc()
  }

  private def reportStatusDifference(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    logger.info("============tracks/:id endpoint status difference=========")
    logger.info(request.toString)
    logger.info(request.headerMap.toString)
    logger.info(s"legacy res : ${legacyRes.toString}")
    logger.info(s"migration res : ${migrationRes.toString}")
    failuresCounter.labels("differentStatusCodes").inc()
    statusCodeDifferenceCounter.labels(legacyRes.statusCode.toString, migrationRes.statusCode.toString).inc()
  }

  private def calcBodyDiffCount(legacyJson: JsObject, migrationJson: JsObject): Int = {
    val extraAttributesCount = (migrationJson.fieldSet.map(_._1) diff legacyJson.fieldSet.map(_._1)).size

    val differentAttributeCount = legacyJson.fields.count {
      case (key, jsValue) =>
        jsValue != migrationJson \ key
    }

    differentAttributeCount + extraAttributesCount
  }

  private def getDifferentHeaders(legacyHeaders: HeaderMap, migrationHeaders: HeaderMap): Seq[String] = {
    val extraHeaders = (migrationHeaders.keys.toSeq diff legacyHeaders.keys.toSeq)

    val filteredLegacyHeaders = legacyHeaders.filter {
      case (key, value) =>
        ignoredHeaders.contains(key) == false
    }

    val differentAttributeCount = filteredLegacyHeaders.filter {
      case (key, value) =>
          migrationHeaders.get(key).isEmpty ||
          value != migrationHeaders.get(key).get
    }.keys.toSeq

    extraHeaders ++ differentAttributeCount
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
