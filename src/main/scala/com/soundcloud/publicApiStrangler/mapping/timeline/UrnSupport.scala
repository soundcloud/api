package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.JsonMapping
import com.soundcloud.jvmkit.module.util.Urn


trait UrnSupport {
  self: JsonMapping =>

  def urn = Urn((json \ "self" \ "urn").as[String])

}
