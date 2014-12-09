package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.util.Await
import com.twitter.finagle.http.MediaType
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.Future
import org.mockito.Mockito._

class AcceptOnlyJsonRequestFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, Response]]
    val filter = new AcceptOnlyJsonRequestFilter
    val request: Request
    lazy val response =
      Await.result(filter(request, next))
  }

  "allows json request" >> {

    for (
      header <- List(
        "application/json",
        "application/javascript",
        "text/json",
        "text/x-json",
        "application/x-json",
        "text/javascript",
        "text/x-javascript",
        "application/x-javascript")
    ) yield {

      s"using the '$header' header" in new Context {
        val request = Request()
        request.accept = header

        val responseFromNextService = mock[Response]
        when(next.apply(request))
          .thenReturn(Future.value(responseFromNextService))

        response mustEqual responseFromNextService
      }
    }

    "without the header and extension" in new Context {
      val request = Request()
      val responseFromNextService = mock[Response]
      when(verified(next).apply(request))
        .thenReturn(Future.value(responseFromNextService))

      response mustEqual responseFromNextService
    }
  }

  "rejects non-json requests with a 406 response" >> {

    "using the suffix" in new Context {
      val request = Request("/test.xml")
      
      response.statusCode mustEqual 406
      verifyNoMoreInteractions(next)
    }

    "using the accept header" in new Context {
      val request = Request()
      request.accept = MediaType.Xml
      
      response.statusCode mustEqual 406
      verifyNoMoreInteractions(next)
    }
  }
}
