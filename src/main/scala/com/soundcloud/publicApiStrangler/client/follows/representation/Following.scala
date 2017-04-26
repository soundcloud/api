package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats._
import org.joda.time.LocalDateTime
import play.api.libs.json.{Json, Reads}

case class Following(id: String, created: LocalDateTime, target: Urn, user: Urn)

object Following {
  implicit val reads: Reads[Following] = Json.reads[Following]
}
