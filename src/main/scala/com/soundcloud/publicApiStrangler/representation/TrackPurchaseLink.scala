package com.soundcloud.publicApiStrangler.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats.urnFormat

case class TrackPurchaseLink(
                              track_urn: Urn,
                              title: Option[String],
                              url: String
                            )

object TrackPurchaseLink {
  implicit val format = Json.format[TrackPurchaseLink]
}
