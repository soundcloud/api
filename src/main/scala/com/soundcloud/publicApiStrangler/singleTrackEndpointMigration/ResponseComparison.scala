package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.Country
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle.http.{HeaderMap, Request, Response, Status}
import com.twitter.util.{NonFatal, Return, Try}
import play.api.data.validation.ValidationError

import scala.collection.Set
import play.api.libs.json._

class ResponseComparison(telemetry: Telemetry) {
  private val logger = SoundCloudLoggerFactory.getLogger(this.getClass.getName)

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
      } else if (migrationRes.status != Status.Ok && migrationRes.status != Status.NotFound) {
        reportUnexpectedMoshiStatus(request, legacyRes, migrationRes)
      } else {
        val (legacyCallback, legacyJson) = pluckCallbackAndData(legacyRes.contentString)
        val (migrationCallback, migrationJson) = pluckCallbackAndData(migrationRes.contentString)

        compareHeaders(legacyRes, migrationRes)
        compareJsonpData(legacyCallback, migrationCallback)
        compareJsonBody(request, legacyRes, migrationRes, legacyJson, migrationJson)
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

  private def compareJsonpData(legacyCallback: Option[String], migrationCallback: Option[String]): Unit = {
    if (legacyCallback != migrationCallback) {
      failuresCounter.labels("jsonpTextFailure").inc()
    }
  }

  private def compareJsonBody(request: Request, legacyRes: Response, migrationRes: Response, legacyJsonString: String, migrationJsonString: String): Unit = {
    val legacyJsonTry = Try(Json.fromString(legacyJsonString).as[JsObject])
    val migrationJsonTry = Try(Json.fromString(migrationJsonString).as[JsObject])

    (legacyJsonTry, migrationJsonTry) match {
      case (Return(legacyJson), Return(migrationJson)) =>
        reportAttributeDifferences(legacyJson, migrationJson)
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

  private def detectAttributesOnlyPresentInOneResponse(legacyJson: JsObject, migrationJson: JsObject): (Set[String], Set[String]) = {
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

    (attributesOnlyInLegacy, attributesOnlyInMigration)
  }

  private def detectAttributesWithDifferentValues(legacyJson: JsObject, migrationJson: JsObject): Set[String] = {
    val legacyAttributes = legacyJson.fieldSet.map(_._1)
    val migrationAttributes = migrationJson.fieldSet.map(_._1)
    val attributesPresentInBothResponses = legacyAttributes.intersect(migrationAttributes)

    attributesPresentInBothResponses.filter {
      attr => {
        val legacyValue = legacyJson \ attr
        val migrationValue = migrationJson \ attr

        val isDifferent = attr match {
          case "comment_count" | "download_count" | "favoritings_count" | "playback_count" =>
            // Counts in Mothership are not reliable, and so Public API Strangler fetches counts from Stitch instead.
            false
          case "downloadable" =>
            // This boolean flag relies on the download count, and as such will also vary in line with the differences
            // between counts from Mothership and Stitch.
            false
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
          case "tag_list" =>
            isTagListDifferent(legacyValue, migrationValue)
          case "available_country_codes" =>
            areAvailableCountryCodesDifferent(legacyValue, migrationValue)
          case "genre" | "description"| "label_name"| "purchase_title"| "track_type"| "release" | "key_signature" =>
            legacyValue.as[JsString].value != migrationValue.as[JsString].value
          case _ =>
            legacyValue != migrationValue
        }

        isDifferent
      }
    }
  }

  private def isTagListDifferent(legacyValue: JsValue, migrationValue: JsValue): Boolean = {
    val legacyString = legacyValue.as[JsString].value
    val migrationString = migrationValue.as[JsString].value

    !legacyString.split("\\s+").sorted.sameElements(migrationString.split("\\s+").sorted)
  }

  private def areAvailableCountryCodesDifferent(legacyValue: JsValue, migrationValue: JsValue): Boolean = {
    // Mothership’s response can contain country codes that are not officially assigned ISO 3166-1 alpha-2
    // country codes. Public API Strangler’s response does not contain such country codes.

    val legacyArray = legacyValue.as[JsArray].value.map(_.as[JsString].value)
    val migrationArray = migrationValue.as[JsArray].value.map(_.as[JsString].value)

    Country.officiallyAssignedAlpha2Codes.intersect(legacyArray.toSet) != migrationArray.toSet
  }

  private def reportAttributeDifferences(legacyJson: JsObject, migrationJson: JsObject): Unit = {
    val (attributesOnlyInLegacy, attributesOnlyInMigration) = detectAttributesOnlyPresentInOneResponse(legacyJson, migrationJson)
    val attributesWithDifferentValues = detectAttributesWithDifferentValues(legacyJson, migrationJson)

    lazy val trackId = (legacyJson \ "id").as[JsNumber].value

    val attributesWithDifferences = attributesOnlyInLegacy ++ attributesOnlyInMigration ++ attributesWithDifferentValues
    if (attributesWithDifferences.nonEmpty) {
      logger.info(s"Found different responses for legacy/migration for track soundcloud:tracks:$trackId")
    }

    attributesOnlyInLegacy.foreach(attributeOnlyPresentInCounter.labels("legacy", _).inc())
    attributesOnlyInMigration.foreach(attributeOnlyPresentInCounter.labels("migration", _).inc())
    attributesWithDifferentValues.foreach(attributeValueDifferentCounter.labels(_).inc())

    val attributesToPrint = attributesWithDifferences.intersect(printableAttributes)
    attributesToPrint.foreach { attr =>
      logger.info(s"soundcloud:tracks:$trackId, attr $attr: legacy = ${legacyJson \ attr}, migration = ${migrationJson \ attr}")
    }
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

  private val printableAttributes = Set(
    "artwork_url",
    "available_country_codes",
    "bpm",
    "comment_count",
    "description",
    "download_count",
    "download_url",
    "downloadable",
    "downloads_remaining",
    "duration",
    "favoritings_count",
    "genre",
    "label",
    "label_name",
    "last_modified",
    "original_content_size",
    "original_format",
    "permalink_url",
    "playback_count",
    "purchase_url",
    "purchase_title",
    "release_day",
    "release_month",
    "release_year",
    "state",
    "stream_url",
    "streamable",
    "tag_list",
    "title",
    "uri",
    "user_favorite",
    "waveform_url",
    "user"
  )

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
