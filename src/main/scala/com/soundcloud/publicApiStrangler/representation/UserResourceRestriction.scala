package com.soundcloud.publicApiStrangler.representation

import play.api.libs.json.JsValue

case class UserResourceRestriction(reason: String)

object UserResourceRestriction {

  def parse(js: JsValue) = {
    (js \ "reason").asOpt[String].map(UserResourceRestriction(_))
  }
}
