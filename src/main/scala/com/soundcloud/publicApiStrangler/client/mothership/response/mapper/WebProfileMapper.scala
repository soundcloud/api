package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.WebProfile
import play.api.libs.json.JsObject

object WebProfileMapper {
  def apply(profiles: List[JsObject]): List[WebProfile] = {
    profiles.map { profile =>
      new WebProfile(
        title = (profile \ "title").asOpt[String],
        service = (profile \ "service").as[String],
        url = (profile \ "url").as[String]
      )
    }
  }
}
