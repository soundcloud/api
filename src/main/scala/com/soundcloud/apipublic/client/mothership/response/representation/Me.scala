package com.soundcloud.apipublic.client.mothership.response.representation

import com.soundcloud.apipublic.service.users.UserUploadQuota
import play.api.libs.json.{JsObject, Json, Writes}

case class Me(
    userRepresentation: UserRepresentation,
    private_tracks_count: Option[Long],
    private_playlists_count: Option[Long],
    primary_email_confirmed: Option[Boolean],
    locale: Option[String],
    quota: Option[UserUploadQuota]
)

object Me {
  implicit val writes = Writes[Me] { me =>
    var jsObject = Json.toJson(me.userRepresentation).as[JsObject]

    jsObject = jsObject ++ Json.obj(
      "quota" -> me.quota,
      "private_tracks_count" -> me.private_tracks_count,
      "private_playlists_count" -> me.private_playlists_count,
      "primary_email_confirmed" -> me.primary_email_confirmed,
      "locale" -> me.locale,
      "upload_seconds_left" -> me.quota.flatMap(_.secondsLeft)
    )

    jsObject
  }
}
