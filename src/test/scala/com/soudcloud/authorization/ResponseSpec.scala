package com.soudcloud.authorization

import com.soundcloud.bff.test.UnitSpecification

class ResponseSpec extends UnitSpecification with Fixtures {

  val body = singleTrack.toString

  "normal response" >> {
    "returns the same string as the response's content" in {
      Response(body).content mustEqual body
    }
    "builds the new response using the specified body" in {
      val response = Response(body).withBody("newbody").build
      response.getContentString mustEqual "newbody"
    }
  }

  "callback response" >> {

    val name = "jQuery111002918246176559478_1407849576700"
    val callbackBody = s"""/**/$name($body);"""

    "returns callback body as the response's content" in {
      Response(callbackBody).content mustEqual body
    }

    "builds the new response using the specified body" in {
      val newBody = "newbody"
      val response = Response(callbackBody).withBody(newBody).build
      response.getContentString mustEqual s"""/**/$name($newBody);"""
    }
  }
}
