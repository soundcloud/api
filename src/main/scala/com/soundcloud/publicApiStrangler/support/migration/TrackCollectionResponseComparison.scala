package com.soundcloud.publicApiStrangler.support.migration

import com.soundcloud.jvmkit.Country
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{HeaderMap, Request, Response, Status}
import com.twitter.util.{NonFatal, Return, Try}
import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.collection.Set

class TrackCollectionResponseComparison(telemetry: Telemetry) {
  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass.getName)

  val counterPrefix = "track_collection"

  val statusCodeDifferenceCounter = telemetry.counter(
    s"${counterPrefix}_endpoint_status_code_difference",
    "counter for response comparison where status codes are different",
    "legacy", "migration"
  )

  val failuresCounter = telemetry.counter(
    s"${counterPrefix}_endpoint_failures",
    "counter for response comparison failures",
    "type"
  )

  val attributeOnlyPresentInCounter = telemetry.counter(
    s"${counterPrefix}_attribute_only_present_in",
    "counter for attributes only present in one of the two responses",
    "response", "attribute"
  )

  val attributeValueDifferentCounter = telemetry.counter(
    s"${counterPrefix}_attribute_value_different",
    "counter for attributes where the values are different",
    "attribute"
  )

  implicit object JsStringReads extends Reads[JsString] {
    def reads(json: JsValue) = json match {
      case JsNull => JsSuccess(JsString(""))
      case s: JsString => JsSuccess(s)
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.jsstring"))))
    }
  }

  def report(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    try {
      if (legacyRes.status != migrationRes.status) {
        reportStatusDifference(request, legacyRes, migrationRes)
      } else {
        compareHeaders(legacyRes, migrationRes)
      }
    } catch {
      case NonFatal(ex) => {
        logger.error(s"Exception occurred during response comparison:", ex)
        failuresCounter.labels("exception_during_comparison").inc()
      }
    }
  }

  private def compareHeaders(legacyRes: Response, migrationRes: Response): Unit = {
    val differentHeaders = getDifferentHeaders(legacyRes.headerMap, migrationRes.headerMap)
    if (differentHeaders.nonEmpty) {
      reportHeaderDifference(legacyRes, migrationRes, differentHeaders)
    }
  }

  private def reportHeaderDifference(legacyRes: Response, migrationRes: Response, differentHeaders: Seq[String]): Unit = {
    failuresCounter.labels("differentHeaderCount").inc()
  }

  def reportJsonFailure(request: Request, legacyRes: Response, migrationRes: Response, legacyResult: Try[JsObject], migrationResult: Try[JsObject]): Unit = {
    failuresCounter.labels("jsonFailure").inc()
  }

  private def reportStatusDifference(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    logger.info(s"status code difference (legacy = ${legacyRes.statusCode}, migration = ${migrationRes.statusCode}) for ${request.method} ${request.path}")
    statusCodeDifferenceCounter.labels(legacyRes.statusCode.toString, migrationRes.statusCode.toString).inc()
  }

  private def getDifferentHeaders(legacyHeaders: HeaderMap, migrationHeaders: HeaderMap): Seq[String] = {
    val extraHeaders = migrationHeaders.keys.toSeq diff legacyHeaders.keys.toSeq

    val filteredLegacyHeaders = legacyHeaders.filter {
      case (key, value) =>
        !ignoredHeaders.contains(key)
    }

    val differentAttributeCount = filteredLegacyHeaders.filter {
      case (key, value) =>
        migrationHeaders.get(key).isEmpty ||
          value != migrationHeaders(key)
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
}
