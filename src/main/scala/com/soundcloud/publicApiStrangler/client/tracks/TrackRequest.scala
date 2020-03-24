package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{Json, Writes}
import com.soundcloud.jvmkit.module.json.UrnFormat._

case class TrackRequest(urn: Urn, secretToken: Option[String])

object TrackRequest {
  implicit val writes: Writes[TrackRequest] = Json.writes[TrackRequest]
}
