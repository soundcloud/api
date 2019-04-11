package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.json.UrnFormat._
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}
import play.api.libs.json.JodaReads._

case class Following(id: String, created: DateTime, target: Urn, user: Urn)

object Following {
  implicit val reads: Reads[Following] = Json.reads[Following]
}
