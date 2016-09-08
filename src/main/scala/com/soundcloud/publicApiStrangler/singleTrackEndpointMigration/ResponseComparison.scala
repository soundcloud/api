package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{HeaderMap, Request, Response, Status}
import com.twitter.util.{Return, Try}
import play.api.libs.json.{JsObject, JsString}

class ResponseComparison(telemetry: Telemetry) {
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

  val attributeOnlyPresentInCounter = telemetry.counter(
    "attribute_only_present_in",
    "counter for attributes only present in one of the two responses",
    "response", "attribute"
  )

  val attributeValueDifferentCounter = telemetry.counter(
    "attribute_value_different",
    "counter for attributes where the values are different",
    "attribute"
  )

  def report(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    if (legacyRes.status != migrationRes.status) {
      reportStatusDifference(request, legacyRes, migrationRes)
    } else if (migrationRes.status != Status.Ok && migrationRes.status != Status.NotFound) {
      reportUnexpectedMoshiStatus(request, legacyRes, migrationRes)
    } else {
      val (legacyCallback, legacyJson) = pluckCallbackAndData(legacyRes.contentString)
      val (migrationCallback, migrationJson) = pluckCallbackAndData(migrationRes.contentString)

      compareHeaders(legacyRes, migrationRes)
      compareJsonpData(legacyCallback, migrationCallback)
      compareJsonBody(request, legacyRes, migrationRes, legacyJson, migrationJson)
    }
  }

  private def compareHeaders(legacyRes: Response, migrationRes: Response): Unit = {
    val differentHeaders = getDifferentHeaders(legacyRes.headerMap, migrationRes.headerMap)
    if (differentHeaders.size > 0) {
      reportHeaderDifference(legacyRes, migrationRes, differentHeaders)
    }
  }

  private def compareJsonpData(legacyCallback: Option[String], migrationCallback: Option[String]): Unit = {
    if (legacyCallback != migrationCallback) {
      failuresCounter.labels("jsonpTextFailure").inc()
    }
  }

  private def compareJsonBody(request: Request, legacyRes: Response, migrationRes: Response, legacyJson: String, migrationJson: String): Unit = {
    val legacyJsonTry = Try(Json.fromString(legacyJson).as[JsObject])
    val migrationJsonTry = Try(Json.fromString(migrationJson).as[JsObject])


    (legacyJsonTry, migrationJsonTry) match {
      case (Return(legacyJson), Return(migrationJson)) => {
        reportAttributesOnlyPresentInOneResponse(legacyJson, migrationJson)
        reportAttributeValueDifferences(legacyJson, migrationJson)
      }
      case (legacyResult, migrationResult) =>
        reportJsonFailure(request, legacyRes, migrationRes, legacyResult, migrationResult)
    }
  }

  /**
    * This method will parse out callback function name and json data if response is a jsonp
    * If response is not json it will return the whole content as data
    */
  private def pluckCallbackAndData(contentString: String): (Option[String], String) = {
    val jsonpRegex = """\/\*\*\/(.*)\((\{.*\})\);""".r

    contentString match {
      case jsonpRegex(callback, json) => (Some(callback), json)
      case _ => (None, contentString)
    }
  }

  private def reportHeaderDifference(legacyRes: Response, migrationRes: Response, differentHeaders: Seq[String]): Unit = {
    failuresCounter.labels("differentHeaderCount").inc()
  }

  def reportJsonFailure(request: Request, legacyRes: Response, migrationRes: Response, legacyResult: Try[JsObject], migrationResult: Try[JsObject]): Unit = {
    failuresCounter.labels("jsonFailure").inc()
  }

  private def reportUnexpectedMoshiStatus(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    failuresCounter.labels("unexpectedMoshiStatusCode").inc()
  }

  private def reportStatusDifference(request: Request, legacyRes: Response, migrationRes: Response): Unit = {
    statusCodeDifferenceCounter.labels(legacyRes.statusCode.toString, migrationRes.statusCode.toString).inc()
  }

  private def reportAttributesOnlyPresentInOneResponse(legacyJson: JsObject, migrationJson: JsObject): Unit = {
    // reposts_count, likes_count
    //    Present in the legacy response for compatibility with the android app. No longer relevant,
    //    and therefore not migrated.
    //
    // download_url, stream_url
    //    Conditionally present in the legacy response, and unconditionally present in the migrated
    //    response. Reproducing the logic for determining whether a track is downloadable or
    //    streamable was deemed not worth it, especially considering that these URLs are trivial to
    //    reconstruct based off the track ID.
    val ignoredLegacyAttributes = Set("reposts_count", "likes_count")
    val ignoredMigrationAttributes = Set("download_url", "stream_url")

    val legacyAttributes = legacyJson.fieldSet.map(_._1)
    val migrationAttributes = migrationJson.fieldSet.map(_._1)
    val attributesOnlyInLegacy = legacyAttributes -- migrationAttributes -- ignoredLegacyAttributes
    val attributesOnlyInMigration = migrationAttributes -- legacyAttributes -- ignoredMigrationAttributes

    attributesOnlyInLegacy.foreach(attr => attributeOnlyPresentInCounter.labels("legacy", attr).inc())
    attributesOnlyInMigration.foreach(attr => attributeOnlyPresentInCounter.labels("migration", attr).inc())
  }

  private def reportAttributeValueDifferences(legacyJson: JsObject, migrationJson: JsObject): Unit = {
    val legacyAttributes = legacyJson.fieldSet.map(_._1)
    val migrationAttributes = migrationJson.fieldSet.map(_._1)
    val attributesPresentInBothResponses = legacyAttributes.intersect(migrationAttributes)

    attributesPresentInBothResponses.foreach(attr => {
      val legacyValue = legacyJson \ attr
      val migrationValue = migrationJson \ attr

      val isDifferent = attr match {
        case "isrc" =>
          // The ISRC is now obtained from Pubmese rather than Mothership. Pubmese is the authoritative
          // source for ISRCs.
          false
        case "permalink_url" =>
          // The legacy response serves permalink URLs with http://, while the migrated response serves
          // them with https:// instead.
          val legacyString = legacyValue.as[JsString].value
          val migrationString = migrationValue.as[JsString].value
          legacyString.replaceFirst("^http://", "https://") != migrationString
        case _ =>
          legacyValue != migrationValue
      }

      if (isDifferent) attributeValueDifferentCounter.labels(attr).inc()
    })
  }

  private def getDifferentHeaders(legacyHeaders: HeaderMap, migrationHeaders: HeaderMap): Seq[String] = {
    val extraHeaders = (migrationHeaders.keys.toSeq diff legacyHeaders.keys.toSeq)

    val filteredLegacyHeaders = legacyHeaders.filter {
      case (key, value) =>
        !ignoredHeaders.contains(key)
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
}
