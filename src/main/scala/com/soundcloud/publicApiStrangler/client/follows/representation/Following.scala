package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.publicApiStrangler.client.follows.util.JsonParsingExtensions.localDateTimeReads
import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Urn.format
import org.joda.time.LocalDateTime
import play.api.libs.json.{Json, Reads, Writes}

case class Following(id: String, created: LocalDateTime, target: Urn, user: Urn)

object Following {
  implicit val reads: Reads[Following] = Json.reads[Following]
}
