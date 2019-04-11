package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.JsValue

/**
  * Finds URNs in expected places in a [[JsValue]]. Can be used in pattern matching or by itself.
  *
  * Expected places are either a top level `urn` element or a `self -> urn` structure.
  */
case object HasUrn {
  def apply(json: JsValue) = unapply(json)

  def unapply(json: JsValue): Option[Urn] = (json \ "urn").asOpt[String].orElse((json \ "self" \ "urn").asOpt[String])
    .flatMap(Urn.parse(_).toOption)
}
