package com.soundcloud.apipublic.subscriptions

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{JsNull, JsValue, Json}
import com.twitter.finagle.http.{Response, Status}

object SubmarineCreatorSubscriptionsResponseMapper {
  def apply(response: Response): Map[Urn, Option[SubmarineCreatorSubscription]] = {
    response.status match {
      case Status.Ok =>
        (Json.parse(response.contentString) \ "data").as[Map[String, JsValue]].map {
          case (urn, json) => {
            val creatorSubscription = json match {
              case JsNull => None
              case json: JsValue =>
                Some(
                  SubmarineCreatorSubscription(
                    (json \ "recurring").asOpt[Boolean].getOrElse(false),
                    Package(
                      (json \ "package" \ "name").as[String],
                      (json \ "package" \ "plan").as[String]
                    )
                  )
                )
            }
            (Urn.parse(urn).get, creatorSubscription)
          }
        }
      case _ => Map.empty
    }
  }
}
