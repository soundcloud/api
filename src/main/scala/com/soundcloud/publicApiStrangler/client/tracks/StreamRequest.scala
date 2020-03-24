package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{Json, Writes}
import com.soundcloud.jvmkit.module.json.UrnFormat._

object StreamRequest {
  implicit val writes: Writes[StreamRequest] = Json.writes[StreamRequest]
}

case class StreamRequest(urn: Urn, secretToken: Option[String], transcodingId: String, protocol: String)
