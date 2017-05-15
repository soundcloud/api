package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{Json, Reads}
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats.urnSeqReads

case class UserUrns(urns: Seq[Urn])

object UserUrns {

  implicit val reads: Reads[UserUrns] = Json.reads[UserUrns]
}
