package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsObject

trait LikesCountSupport {

  def likesByUrn: Map[Urn, Int]


}
