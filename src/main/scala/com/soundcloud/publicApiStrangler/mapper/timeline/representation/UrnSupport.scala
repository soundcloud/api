package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn

trait UrnSupport {
  self: JsonMapping =>

  def urn = (json \ "self" \ "urn").as[Urn]
}
