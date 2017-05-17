package com.soundcloud.publicApiStrangler.client

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.http.Status.Successful
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}

object ResponseHandlers {

  object ListResponse {
    def apply(response: Response): List[JsObject] = JsValueResponse(response).as[List[JsObject]]
  }

  object StringSetResponse {
    def apply(response: Response): Set[String] = JsValueResponse(response).as[Set[String]]
  }

  object SingleItem {
    def apply(response: Response): JsObject = OptionalSingleItem(response).getOrElse(invalidResponse(response))
  }

  object UnitResponse {
    def apply(response: Response): Unit =
      response.status match {
        case Successful(_) =>
        case _ => invalidResponse(response)
      }
  }

  object BooleanByStatusResponse {
    def apply(response: Response): Boolean =
      response.status match {
        case Successful(_) => true
        case Status.NotFound => false
        case _ => invalidResponse(response)
      }
  }

  object JsValueResponse {
    def apply(response: Response): JsValue =
      response.status match {
        case Successful(_) => Json.parse(response.contentString)
        case _ => invalidResponse(response)
      }
  }

  object OptionalSingleItem {
    def apply(response: Response): Option[JsObject] =
      response.status match {
        case Status.NotFound => None
        case Successful(_) => {
          Json.parse(response.contentString) match {
            case JsNull => None
            case json: JsObject => Some(json)
            case _ => invalidResponse(response)
          }
        }
        case _ => invalidResponse(response)
      }
  }

  def invalidResponse(response: Response): Nothing = {
    throw new IllegalStateException(s"Invalid response: status=${response.statusCode},body=${response.contentString},headers=${response.headerMap}")
  }

}
