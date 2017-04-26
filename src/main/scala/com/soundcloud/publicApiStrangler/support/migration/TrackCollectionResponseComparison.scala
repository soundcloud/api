package com.soundcloud.publicApiStrangler.support.migration

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.scalakit.notifier.AirbrakeNotifier
import com.twitter.finagle.http.{HeaderMap, Request, Response}
import com.twitter.util.{NonFatal, Return, Try}
import play.api.data.validation.ValidationError
import play.api.libs.json._

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
        compareBodies(request.uri, legacyRes, migrationRes)
      }
    } catch {
      case NonFatal(ex) => {
        logger.error(s"Exception occurred during response comparison:", ex)
        failuresCounter.labels("exception_during_comparison").inc()
      }
    }
  }

  private def compareBodies(path: String, legacyRes: Response, migrationRes: Response) = {
    val legacyJsonTry = Try(Json.parse(legacyRes.getContentString()).as[JsValue])
    val migrationJsonTry = Try(Json.parse(migrationRes.getContentString()).as[JsValue])

    (legacyJsonTry, migrationJsonTry) match {
      case (Return(legacyJson), Return(migrationJson)) => {
        (legacyJson, migrationJson) match {
          case (legacyTracks: JsArray, migrationTracks: JsArray) => {
            if (legacyTracks.value.size != migrationTracks.value.size) {
              failuresCounter.labels("legacyAndMigrationArrayDifferentCollectionSize").inc()
              reportDifferentJson(
                s"Different amount of tracks without linked partitioning for path ${path}",
                legacyTracks,
                migrationTracks)
            }
          }
          case (legacyContent: JsObject, migrationContent: JsObject) => {
            val legacyTracks = (legacyContent \ "collection").as[JsArray]
            val migrationTracks = (legacyContent \ "collection").as[JsArray]
            if (legacyTracks.value.size != migrationTracks.value.size) {
              failuresCounter.labels("legacyAndMigrationObjectDifferentCollectionSize").inc()
              reportDifferentJson(
                s"Different amount of tracks with linked partitioning for path ${path}",
                legacyTracks,
                migrationTracks)
            }
          }
          case (legacyJson: JsObject, migrationJson: JsArray) => {
            failuresCounter.labels("legacyObjectMigrationArray").inc()
            reportDifferentJson(s"Legacy response is linked partitioned by migration response isn't for path ${path}",
              legacyJson,
              migrationJson)
          }
          case (legacyJson: JsArray, migrationJson: JsObject) => {
            failuresCounter.labels("legacyArrayMigrationObject").inc()
            reportDifferentJson(s"Legacy response is not linked partitioned by migration response is for path ${path}",
              legacyJson,
              migrationJson)
          }
        }
      }
      case (_, Return(_)) => {
        failuresCounter.labels("legacyFailedToParse").inc()
      }
      case (Return(_), _) => {
        failuresCounter.labels("migrationFailedToParse").inc()
      }
      case (_, _) => {
        failuresCounter.labels("legacyFailedToParse").inc()
        failuresCounter.labels("migrationFailedToParse").inc()
      }
    }
  }


  private def reportDifferentJson(message: String, legacyJson: JsValue, migrationJson: JsValue) = {
    val fullMessage =
      s"""
         |${message}
         |Legacy response was:
         |  ${Json.prettyPrint(legacyJson)}
         |
         |Migration response was:
         |  ${Json.prettyPrint(migrationJson)}
                """.stripMargin
    logger.info(s"TrackCollectionResponseComparison: $fullMessage")
    AirbrakeNotifier.notify(fullMessage)
  }

  private def compareHeaders(legacyRes: Response, migrationRes: Response): Unit = {
    val differentHeaders = getDifferentHeaders(legacyRes.headerMap, migrationRes.headerMap)
    if (differentHeaders.nonEmpty) {
      reportHeaderDifference(legacyRes, migrationRes, differentHeaders)
    }
  }

  private def reportHeaderDifference(legacyRes: Response, migrationRes: Response, differentHeaders: Seq[String]): Unit = {
    logger.info(s"TrackCollectionResponseComparison: reporting header difference")
    failuresCounter.labels("differentHeaderCount").inc()
  }

  private def reportStatusDifference(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    logger.info(s"TrackCollectionResponseComparison: status code difference (legacy = ${legacyRes.statusCode}, migration = ${migrationRes.statusCode}) for ${request.method} ${request.path}")
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
