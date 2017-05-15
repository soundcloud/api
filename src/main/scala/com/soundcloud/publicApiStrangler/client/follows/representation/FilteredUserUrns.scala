package com.soundcloud.publicApiStrangler.client.follows.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats.urnSetReads
import play.api.libs.json.{Json, Reads}

case class FilteredUserUrns(included: Set[Urn], excluded: Set[Urn])

object FilteredUserUrns {

  implicit val reads: Reads[FilteredUserUrns] = Json.reads[FilteredUserUrns]
}
