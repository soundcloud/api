package com.soundcloud.publicApiStrangler.mapper.spotlight

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import play.api.libs.json.JsValue

class SpotlightMapperSpec extends UnitSpecification {
  trait Context extends Scope {
    val spotlight: Spotlight
  }

  "#apply" >> {
    trait ApplyContext extends Context {
      val json: JsValue

      override lazy val spotlight = SpotlightMapper(json)
    }

    "when the response contains items" >> {
      trait ResponseWithItems extends ApplyContext {
        override lazy val json = Fixtures.okidokiSpotlightWithItems
      }

      "maps items" in new ResponseWithItems {
        spotlight.items must haveLength(5)
      }
    }

    "when the response is empty" >> {
      trait EmptyResponse extends ApplyContext {
        override lazy val json = Fixtures.okidokiSpotlightEmpty
      }

      "maps items" in new EmptyResponse {
        spotlight.items must haveLength(0)
      }
    }
  }
}
