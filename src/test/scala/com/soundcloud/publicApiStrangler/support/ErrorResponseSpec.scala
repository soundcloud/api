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
      "link" -> "https://developers.soundcloud.com/docs/api/explorer/open-api"
    )
  }

  "maps error message to object" in new Scope {
    val response = ErrorResponse(Status.Forbidden, "Something")
    response.statusCode === 403
    Json.parse(response.contentString) === Json.obj(
      "code" -> 403,
      "message" -> "Something",
      "link" -> "https://developers.soundcloud.com/docs/api/explorer/open-api",
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
      "link" -> "https://developers.soundcloud.com/docs/api/explorer/open-api",
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
      "link" -> "https://developers.soundcloud.com/docs/api/explorer/open-api",
      "status" -> "403 - Forbidden",
      "error" -> null,
      "errors" -> Json.arr(),
      "else" -> "other"
    )
  }

  "links to issue tracker when an unexpected error" in new Scope {
    val response = ErrorResponse(Status.InternalServerError)
    response.statusCode === 500
    Json.parse(response.contentString) === Json.obj(
      "code" -> 500,
      "message" -> "",
      "link" -> "https://github.com/soundcloud/api",
      "status" -> "500 - Internal Server Error",
      "error" -> null,
      "errors" -> Json.arr()
    )
  }

  "links to rate limiting article when 429" in new Scope {
    val response = ErrorResponse(Status.TooManyRequests)
    response.statusCode === 429
    Json.parse(response.contentString) === Json.obj(
      "code" -> 429,
      "message" -> "",
      "link" -> "https://developers.soundcloud.com/docs/api/rate-limits#errors",
      "status" -> "429 - Too Many Requests",
      "error" -> null,
      "errors" -> Json.arr()
    )
  }
}
