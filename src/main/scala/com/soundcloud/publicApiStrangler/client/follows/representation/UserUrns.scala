package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.Urn.format
import play.api.libs.json.{Json, Reads}

case class UserUrns(urns: Seq[Urn])

object UserUrns {

  implicit val reads: Reads[UserUrns] = Json.reads[UserUrns]
}
