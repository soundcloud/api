package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.Error
import play.api.libs.json.{JsObject, JsValue, Json}

object ErrorParser {
  case class MoshiError(message: String, subject: Option[String], status: Option[Int]) {
    def toErrorMessage = {
      val sub = subject.map(_ + " ").getOrElse("")
      val stat = status.map(_ + ": ").getOrElse("")
      s"$stat$sub$message"
    }
  }

  object MoshiError {
    implicit val reads = Json.reads[MoshiError]
  }

  def parse(body: String): Set[Error] = parse(Json.parse(body))

  def parse(body: JsValue): Set[Error] = {
    val errorsJson = (body \ "errors")

    val isMoshiError = (errorsJson \\ "message").size > 0
    val possibleGenericResponseErrors = errorsJson.asOpt[Set[String]]
    val defaultError = Set()

    if (isMoshiError) {
      errorsJson.as[Set[MoshiError]].map(e => Error(e.toErrorMessage))
    } else {
      possibleGenericResponseErrors.getOrElse(defaultError).map(Error(_))
    }
  }

  // expects json in the following format:
  // {"errors":{"asset_data":"Require either asset_data parameter, or uid and original_filename parameters."}}
  def parseMoshiError(body: JsValue): Set[Error] = {
    val errors = body.\("errors").asOpt[JsObject]
    errors
      .map {
        _.fields.map({ case (key, value) => Error(s"$key ${value.as[String]}") }).toSet
      }
      .getOrElse(Set.empty)
  }
}
