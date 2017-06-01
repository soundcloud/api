package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation.WebProfile
import play.api.libs.json.JsObject

object WebProfileMapper {
  def apply(profiles: List[JsObject]): List[WebProfile] = {
    profiles.map {
      profile =>
        new WebProfile(title = (profile \ "title").asOpt[String],
          service = (profile \ "service").as[String],
          url = (profile \ "url").as[String])
    }
  }
}
