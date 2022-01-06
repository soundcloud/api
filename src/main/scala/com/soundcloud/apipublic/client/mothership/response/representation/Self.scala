package com.soundcloud.apipublic.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.json.play.UrnFormat._

case class Self(urn: Urn, url: String)

object Self {
  implicit val reads = Json.reads[Self]
}
