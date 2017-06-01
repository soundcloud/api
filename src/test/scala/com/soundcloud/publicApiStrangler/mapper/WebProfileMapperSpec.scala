package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import play.api.libs.json.JsObject

class WebProfileMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    lazy val webProfilesJson = Fixtures.moshiWebProfiles

    lazy val webProfiles = WebProfileMapper(webProfilesJson.as[List[JsObject]])
  }

  "returns a list of web profiles" in new Context {
    webProfiles.size ==== 2
  }

  "maps the title" in new Context {
    webProfiles.head.title ==== Some("Songkick")
    webProfiles.last.title ==== None
  }

  "maps the service" in new Context {
    webProfiles.head.service ==== "songkick"
    webProfiles.last.service ==== "twitter"
  }

  "maps the url" in new Context {
    webProfiles.head.url ==== "http://www.songkick.com/mortice"
    webProfiles.last.url ==== "http://twitter.com/rentalcustard"
  }
}
