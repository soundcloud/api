package com.soundcloud.publicApiStrangler

import com.soundcloud.testutilities.SpinningUpAppSupport
import com.twitter.finagle.http.Status
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class TrackStreamEndpointSpec extends Specification with SpinningUpAppSupport {

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
