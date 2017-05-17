package com.soundcloud.publicApiStrangler.mapper.spotlight

import com.soundcloud.publicApiStrangler.mapper.UnhandledResponseException
import com.soundcloud.publicApiStrangler.representation.spotlight.Spotlight
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsNull, JsValue}

class SpotlightResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    lazy val spotlightResponseMapper = new SpotlightResponseMapper

    val statusCode: Int
    val json: JsValue

    lazy val response = jsonResponse(Status(statusCode), json)

    lazy val spotlight = spotlightResponseMapper(response)
  }

  "when statusCode is successful" >> {
    trait SuccessfulContext extends Context {
      override lazy val statusCode = 200
      override lazy val json = Fixtures.okidokiSpotlightWithItems
    }

    "returns Spotlight" in new SuccessfulContext {
      spotlight must beAnInstanceOf[Spotlight]
    }
  }

  "when statusCode is not successful" >> {
    trait UnsuccessfulContext extends Context {
      override lazy val statusCode = 500
      override lazy val json = JsNull
    }

    "throws an UnhandledResponseException" in new UnsuccessfulContext {
      spotlight must throwAn[UnhandledResponseException]
    }
  }

}
