package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.{JsString, JsValue, Json}

object ErrorResponse {
  def notFound(error: String = "") = apply(Status.NotFound, error)
  def badRequest(error: String = "") = apply(Status.BadRequest, error)
  def forbidden(error: String = "") = apply(Status.Forbidden, error)
  def unprocessableEntity(error: String = "") = apply(Status.UnprocessableEntity, error)

  def apply(
      status: Status,
      message: String = "",
      extraAttributes: Option[Map[String, JsValue]] = None,
      errorDetails: Option[Map[String, JsValue]] = None
  ): Response = {
    val deprecatedErrorDetails =
      errorDetails.orElse(if (message.isEmpty) None else Some(Map("error_message" -> JsString(message))))

    new JsonResponseBuilder()
      .status(status)
      .body(
        Json.stringify(
          Json.obj(
            "code" -> status.code,
            "message" -> message,
            "link" -> linkForCode(status.code),
            // all the below properties exist for backward compatibility only
            "status" -> s"${status.code} - ${status.reason}",
            "errors" -> deprecatedErrorDetails.toList,
            "error" -> null
          ) ++ Json.toJsObject(extraAttributes.getOrElse(Map.empty))
        )
      )
      .build
  }

  private def linkForCode(code: Int): String = code match {
    case 429 => "https://developers.soundcloud.com/docs/api/rate-limits#errors"
    case c if c >= 500 => "https://github.com/soundcloud/api"
    case c if c >= 400 => "https://developers.soundcloud.com/docs/api/explorer/open-api"
  }
}
