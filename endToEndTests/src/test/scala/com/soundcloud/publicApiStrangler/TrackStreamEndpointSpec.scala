package com.soundcloud.publicApiStrangler

import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.testutilities.{GratisMusikDiebstahl, SpinningUpAppSupport}
import com.twitter.finagle.http.Status

class TrackStreamEndpointSpec extends UnitSpecification with SpinningUpAppSupport {

  "Public API Strangler" should {

    trait Context extends Scope {
      val server = TestServer("strangler", 5000)
    }

    "transparently pass on 302 redirects from Public API" in new Context {

      val response = server.get(s"/tracks/56605565/stream?client_id=40ccfee680a844780a41fbe23ea89934")

      response.status ==== Status.Found.code
      response.location ==== "https://api.soundcloud.com/streams-endpoint"
    }

  }

}
