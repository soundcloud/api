package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{MediaType, Method, Request}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

class AcceptOnlyJsonRequestFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, RouterResponse]]
    val expectedAcceptHeader = "application/json"
  }

  "strips format query parameter when rollout is on" in new Context {
    val filter = new AcceptOnlyJsonRequestFilter(() => Future.True)
    val request = Request("/test", "format" -> "xml")

    val responseFromNextService = mock[RouterResponse]
    when(next.apply(like[Request] {
      case req =>
        req.params.get("format") must beNone
    })).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "doesn't modify incoming request when rollout is off" in new Context {
    val filter = new AcceptOnlyJsonRequestFilter(() => Future.False)
    val request = Request("/test", "format" -> "xml")

    val responseFromNextService = mock[RouterResponse]
    when(next.apply(request)).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "doesn't modify incoming request if request method is NOT GET" in new Context {
    val contentString = "somecontentHere--"
    val filter = new AcceptOnlyJsonRequestFilter(() => Future.True)
    val request = Request("/test", "format" -> "xml")
    request.setContentString(contentString)
    request.method_=(Method.Put)

    val responseFromNextService = mock[RouterResponse]
    when(next.apply(like[Request] {
      case req =>
        req ==== request
        req.contentString ==== contentString
    })).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
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

        val filter = new AcceptOnlyJsonRequestFilter(() => Future.False)

        val responseFromNextService = mock[RouterResponse]
        when(next.apply(like[Request] {
          case req =>
            req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
        })).thenReturn(Future.value(responseFromNextService))

        Await.result(filter(request, next)) mustEqual responseFromNextService
      }
    }

    "using the json suffix" in new Context {
      val request = Request("/test.json")
      val filter = new AcceptOnlyJsonRequestFilter(() => Future.False)

      val responseFromNextService = mock[RouterResponse]
      when(next.apply(like[Request] {
        case req =>
          req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
      })).thenReturn(Future.value(responseFromNextService))

      Await.result(filter(request, next)) mustEqual responseFromNextService
    }

    "without the header and extension" in new Context {
      val request = Request()
      val filter = new AcceptOnlyJsonRequestFilter(() => Future.False)
      val responseFromNextService = mock[RouterResponse]

      when(next.apply(like[Request] {
        case req =>
          req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
      })).thenReturn(Future.value(responseFromNextService))

      Await.result(filter(request, next)) mustEqual responseFromNextService
    }
  }

  "sets accept header to application/json for XML+* requests" in new Context {
    val request = Request()
    request.accept = "application/xml;q=0.8,*/*;q=0.5"
    val filter = new AcceptOnlyJsonRequestFilter(() => Future.False)

    val responseFromNextService = mock[RouterResponse]
    when(next.apply(like[Request] {
      case req =>
        req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
    })).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "rejects non-json requests with a 406 response" >> {

    "using the suffix" in new Context {
      val request = Request("/test.xml")
      val filter = new AcceptOnlyJsonRequestFilter(() => Future.False)

      Await.result(filter(request, next)).statusCode mustEqual 406
      verifyNoMoreInteractions(next)
    }

    "using the accept header" in new Context {
      val request = Request()
      request.accept = MediaType.Xml
      val filter = new AcceptOnlyJsonRequestFilter(() => Future.False)

      Await.result(filter(request, next)).statusCode mustEqual 406
      verifyNoMoreInteractions(next)
    }
  }
}
