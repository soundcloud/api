package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures

class BuilderResponseSpec extends UnitSpecification with Fixtures {

  val body = singleTrack.toString

  "normal response" >> {
    "returns the same string as the response's content" in {
      BuilderResponse(body).content mustEqual body
    }
    "builds the new response using the specified body" in {
      val response = BuilderResponse(body).withBody("newbody").build
      response.getContentString mustEqual "newbody"
    }
  }

  "callback response" >> {

    val name = "jQuery111002918246176559478_1407849576700"
    val callbackBody = s"""/**/$name($body);"""

    "returns callback body as the response's content" in {
      BuilderResponse(callbackBody).content mustEqual body
    }

    "builds the new response using the specified body" in {
      val newBody = "newbody"
      val response = BuilderResponse(callbackBody).withBody(newBody).build
      response.getContentString mustEqual s"""/**/$name($newBody);"""
    }
    
    "supports response with parenthesis" in {
      val body = "this is a (response) with parenthesis"
      val callbackBody = s"""/**/jsonp1407857287982($body);"""
      BuilderResponse(callbackBody).content mustEqual body
    }
  }
}
