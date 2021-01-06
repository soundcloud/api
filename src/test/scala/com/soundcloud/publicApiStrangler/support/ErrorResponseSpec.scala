package com.soundcloud.publicApiStrangler.support

import com.twitter.finagle.http.Status
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.{JsString, Json}

class ErrorResponseSpec extends Specification {
  "creates a representation of the error with error details (deprecated)" in new Scope {
    val response = ErrorResponse(Status.Forbidden, "Message", None, Some(Map("some_key" -> JsString("Something"))))
    response.statusCode === 403
    Json.parse(response.contentString) === Json.obj(
      "code" -> 403,
      "message" -> "Message",
      "status" -> "403 - Forbidden",
      "error" -> null,
      "errors" -> Json.arr(Json.obj("some_key" -> "Something")),
      "link" -> "https://github.com/soundcloud/api"
    )
  }

  "maps error message to object" in new Scope {
    val response = ErrorResponse(Status.Forbidden, "Something")
    response.statusCode === 403
    Json.parse(response.contentString) === Json.obj(
      "code" -> 403,
      "message" -> "Something",
      "link" -> "https://github.com/soundcloud/api",
      "status" -> "403 - Forbidden",
      "error" -> null,
      "errors" -> Json.arr(Json.obj("error_message" -> "Something"))
    )
  }

  "does not include error object when no message" in new Scope {
    val response = ErrorResponse(Status.Forbidden)
    response.statusCode === 403
    Json.parse(response.contentString) === Json.obj(
      "code" -> 403,
      "message" -> "",
      "link" -> "https://github.com/soundcloud/api",
      "status" -> "403 - Forbidden",
      "error" -> null,
      "errors" -> Json.arr()
    )
  }

  "adds extra attributes" in new Scope {
    val response = ErrorResponse(Status.Forbidden, "", Some(Map("else" -> JsString("other"))))
    response.statusCode === 403
    Json.parse(response.contentString) === Json.obj(
      "code" -> 403,
      "message" -> "",
      "link" -> "https://github.com/soundcloud/api",
      "status" -> "403 - Forbidden",
      "error" -> null,
      "errors" -> Json.arr(),
      "else" -> "other"
    )
  }
}
