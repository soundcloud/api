package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.json.play.UrnFormat._

case class TrackPurchaseLink(
    track_urn: Urn,
    title: Option[String],
    url: String
)

object TrackPurchaseLink {
  implicit val format = Json.format[TrackPurchaseLink]
}
