package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.json.UrnFormat._

case class Self(urn: Urn, url: String)

case class Email(self: Self,
                 address: Option[String],
                 bounced: Option[Boolean],
                 confirmed: Option[Boolean],
                 confirmed_at: Option[String],
                 created_at: Option[String],
                 primary: Option[Boolean])

object Self {
  implicit val reads = Json.reads[Self]
}

object Email {
  implicit val reads = Json.reads[Email]
}
