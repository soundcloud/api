package com.soundcloud.apipublic.filter

import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{MediaType, Method, Request, Response}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{when, _}

class AcceptOnlyJsonRequestFilterSpec extends UnitSpecification {
  trait Context extends Scope {
    val next = mock[Service[Request, Response]]
    val expectedAcceptHeader = "application/json"
  }

  "doesn't strip the format query parameter if the value is not xml" in new Context {
    val filter = new AcceptOnlyJsonRequestFilter
    val request = Request("/test", "format" -> "json")

    val responseFromNextService = mock[Response]
    when(next.apply(like[Request] {
      case req =>
        req.params.get("format") must beSome("json")
    })).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "strips format query parameter if the value is xml" in new Context {
    val filter = new AcceptOnlyJsonRequestFilter
    val request = Request("/test", "format" -> "xml")

    val responseFromNextService = mock[Response]
    when(next.apply(like[Request] {
      case req =>
        req.params.get("format") must beNone
    })).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "doesn't modify incoming request if request method is NOT GET" in new Context {
    val contentString = "somecontentHere--"
    val filter = new AcceptOnlyJsonRequestFilter
    val request = Request("/test", "format" -> "xml")
    request.setContentString(contentString)
    request.method_=(Method.Put)

    val responseFromNextService = mock[Response]
    when(next.apply(like[Request] {
      case req =>
        req ==== request
        req.contentString ==== contentString
    })).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "allows json request" >> {
    for (header <- List(
        "application/json",
        "application/javascript",
        "text/json",
        "text/x-json",
        "application/x-json",
        "text/javascript",
        "text/x-javascript",
        "application/x-javascript",
        "*",
        "*/*"
      )) yield {
      s"using the '$header' header" in new Context {
        val request = Request()
        request.accept = header

        val filter = new AcceptOnlyJsonRequestFilter

        val responseFromNextService = mock[Response]
        when(next.apply(like[Request] {
          case req =>
            req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
        })).thenReturn(Future.value(responseFromNextService))

        Await.result(filter(request, next)) mustEqual responseFromNextService
      }
    }

    "using the json suffix" in new Context {
      val request = Request("/test.json")
      val filter = new AcceptOnlyJsonRequestFilter

      val responseFromNextService = mock[Response]
      when(next.apply(like[Request] {
        case req =>
          req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
      })).thenReturn(Future.value(responseFromNextService))

      Await.result(filter(request, next)) mustEqual responseFromNextService
    }

    "without the header and extension" in new Context {
      val request = Request()
      val filter = new AcceptOnlyJsonRequestFilter
      val responseFromNextService = mock[Response]

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
    val filter = new AcceptOnlyJsonRequestFilter

    val responseFromNextService = mock[Response]
    when(next.apply(like[Request] {
      case req =>
        req.acceptMediaTypes ==== Seq(expectedAcceptHeader)
    })).thenReturn(Future.value(responseFromNextService))

    Await.result(filter(request, next)) mustEqual responseFromNextService
  }

  "rejects non-json requests with a 406 response" >> {
    "using the suffix" in new Context {
      val request = Request("/test.xml")
      val filter = new AcceptOnlyJsonRequestFilter

      Await.result(filter(request, next)).statusCode mustEqual 406
      verifyNoMoreInteractions(next)
    }

    "using the accept header" in new Context {
      val request = Request()
      request.accept = MediaType.Xml
      val filter = new AcceptOnlyJsonRequestFilter

      Await.result(filter(request, next)).statusCode mustEqual 406
      verifyNoMoreInteractions(next)
    }
  }
}
