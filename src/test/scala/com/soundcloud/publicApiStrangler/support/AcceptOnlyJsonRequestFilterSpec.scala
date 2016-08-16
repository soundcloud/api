package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{MediaType, Request}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

class AcceptOnlyJsonRequestFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, RouterResponse]]
    val filter = new AcceptOnlyJsonRequestFilter
    val request: Request
    def response =
      Await.result(filter(request, next))

    val expectedAcceptHeader = "application/json"
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
        "application/x-javascript",
        "*",
        "*/*")
    ) yield {

      s"using the '$header' header" in new Context {
        val request = Request()
        request.accept = header

        val responseFromNextService = mock[RouterResponse]
        when(next.apply(like[Request]{
          case req =>
            req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
        })).thenReturn(Future.value(responseFromNextService))

        response mustEqual responseFromNextService
      }
    }

    "using the json suffix" in new Context {
      val request = Request("/test.json")

      val responseFromNextService = mock[RouterResponse]
      when(next.apply(like[Request]{
        case req =>
          req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
      })).thenReturn(Future.value(responseFromNextService))

      response mustEqual responseFromNextService
    }

    "without the header and extension" in new Context {
      val request = Request()
      val responseFromNextService = mock[RouterResponse]
      when(next.apply(like[Request]{
        case req =>
          req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
      })).thenReturn(Future.value(responseFromNextService))

      response mustEqual responseFromNextService
    }
  }

  "sets accept header to application/json for XML+* requests" in new Context {
    val request = Request()
    request.accept = "application/xml;q=0.8,*/*;q=0.5"

    val responseFromNextService = mock[RouterResponse]
    when(next.apply(like[Request]{
      case req =>
        req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
    })).thenReturn(Future.value(responseFromNextService))

    response mustEqual responseFromNextService
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
