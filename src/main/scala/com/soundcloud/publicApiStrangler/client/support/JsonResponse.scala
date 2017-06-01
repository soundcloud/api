package com.soundcloud.publicApiStrangler.client.support

import com.twitter.finagle.http.{HeaderMap, MediaType, Response, Status}
import play.api.libs.json.{JsNull, JsUndefined, JsValue, Json}

import scala.util.control.NonFatal

case class JsonResponse(status: Status, body: Either[JsUndefined, JsValue], headers: HeaderMap = HeaderMap.apply())

object JsonResponse {

  def from[T](response: Response): JsonResponse = {
    contentType(response) match {
      case Some(MediaType.Json) =>
        new JsonResponse(
          response.status,
          Right(jsonFrom(response)),
          response.headerMap
        )

      case Some(contentType) =>
        new JsonResponse(
          response.status,
          Left(JsUndefined(s"Invalid content type: $contentType")),
          response.headerMap
        )

      case None =>
        new JsonResponse(
          response.status,
          Left(JsUndefined(s"No content type set")),
          response.headerMap
        )
    }
  }

  def stringify(json: Either[JsUndefined, JsValue]) = {
    json match {
      case Left(undefined) => undefined.toString
      case Right(jsValue) => Json.stringify(jsValue)
    }
  }

  private def contentType(response: Response): Option[String] = for {
    contentType <- response.contentType
    actualContentType <- contentType.split(";", 2).headOption
  } yield actualContentType.trim.toLowerCase

  /**
    * Parses the body content of a Response as `JsValue`.
    * This uses the InputStream and prevents unnecessary conversion to a String.
    * So if you deal with Http Responses it is advised to use this instead of fromString(String)
    *
    * @param response Http Response
    * @return JsValue parsed from response body.
    */
  private def jsonFrom(response: Response): JsValue = {
    val inputStream = response.getInputStream()
    try {
      Json.parse(inputStream)
    } catch {
      case NonFatal(e) => {
        if (response.contentString.trim.isEmpty) {
          JsNull
        } else {
          throw new IllegalArgumentException(s"Not valid JSON: \n====\n${response.contentString}\n====", e)
        }
      }
    }
    finally {
      inputStream.close()
    }
  }
}
