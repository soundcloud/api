package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.util.Await
import com.twitter.finagle.http.MediaType
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.Future

class RejectXmlRequestFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, Response]]
    val filter = new RejectXmlRequestFilter
    val request: Request
    lazy val response =
      Await.result(filter(request, next))
  }

  "rejects xml requests with a 406 response" >> {

    "using the suffix" in new Context {
      val request = Request("/test.xml")
      response.statusCode mustEqual 406
    }

    "using the accept header" in new Context {
      val request = Request()
      request.accept = MediaType.Xml
      response.statusCode mustEqual 406
    }
  }

  "allows non-xml request" in new Context {
    val request = Request()
    val responseFromNextService = mock[Response]
    when(verified(next).apply(request))
      .thenReturn(Future.value(responseFromNextService))

    response mustEqual responseFromNextService
  }
}
