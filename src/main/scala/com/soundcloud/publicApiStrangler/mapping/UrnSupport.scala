package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue


trait UrnSupport {

  def json: JsValue

  def urn = Urn((json \ "self" \ "urn").as[String])

}
