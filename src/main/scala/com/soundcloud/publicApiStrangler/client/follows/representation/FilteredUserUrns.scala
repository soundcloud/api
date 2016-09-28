package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Urn.{format => urnReads}
import play.api.libs.json.{Json, Reads}

case class FilteredUserUrns(included: Set[Urn], excluded: Set[Urn])

object FilteredUserUrns {

  implicit val reads: Reads[FilteredUserUrns] = Json.reads[FilteredUserUrns]
}
