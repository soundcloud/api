package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{Json, Reads}

case class FilteredUserUrns(included: Set[Urn], excluded: Set[Urn])

object FilteredUserUrns {
  implicit val reads: Reads[FilteredUserUrns] = Json.reads[FilteredUserUrns]
}
