package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.Urn
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Reads}

case class Playlist(userUrn: Urn, secretToken: String)

object Playlist {
  implicit val reads: Reads[Playlist] = new Reads[Playlist] {
    def reads(json: JsValue): JsResult[Playlist] =
      JsSuccess(
        Playlist(
          secretToken = (json \ "secret_token").as[String],
          userUrn = Urn((json \ "user" \ "urn").as[String])
        )
      )
  }
}
