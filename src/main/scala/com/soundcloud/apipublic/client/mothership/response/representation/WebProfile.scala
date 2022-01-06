package com.soundcloud.apipublic.client.mothership.response.representation

import play.api.libs.json.{JsSuccess, Json, Reads}

case class WebProfile(
    created_at: Option[String],
    id: Option[Int],
    kind: String,
    service: Option[String],
    title: Option[String],
    url: String,
    username: Option[String]
)

object WebProfile {
  implicit val reads = Reads[WebProfile] { json =>
    JsSuccess(
      WebProfile(
        created_at = (json \ "created_at").asOpt[String],
        id = (json \ "id").asOpt[Int],
        kind = "web-profile",
        service = (json \ "service").asOpt[String],
        title = (json \ "title").asOpt[String],
        url = (json \ "url").as[String],
        username = (json \ "username").asOpt[String]
      )
    )
  }
  implicit val writes = Json.writes[WebProfile]
}
