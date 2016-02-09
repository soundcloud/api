package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}

class DispatchToMothershipHandlerSpec extends UnitSpecification {
  "dispatches requests to the mothership" >> {

    trait Context extends Scope {
      val mothershipClient = mock[Service[Request, Response]]
      val handler = new DispatchToMothershipHandler(mothershipClient)

      val response = Response(Status.Ok)
      response.headerMap.add("header1", "valueHeader1").add("header2", "valueHeader2")
      response.contentString = "body content"

      val request = Request(Version.Http11, Method.Connect, "/")
      val handlerRequest = new HandlerRequest(AlwaysMatchesPathMatcher, request)
    }

    "returns the response verbatim" >> {
      "for defaultHandling" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        Await.result(handler(handlerRequest)) must be_==(response)
      }

      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        val responseFromBuilder = Await.result(handler.dispatch(request)).build
        responseFromBuilder.getStatusCode() ==== response.getStatusCode()
        responseFromBuilder.headerMap.get("header1") ==== Some("valueHeader1")
        responseFromBuilder.headerMap.get("header2") ==== Some("valueHeader2")
        responseFromBuilder.getContentString() ==== "body content"
      }

    }

    "returns 500 for failed requests" >> {
      "for defaultHandling" in new Context {
        mothershipClient(any[Request]) returns (Future.exception(new IllegalStateException))
        Await.result(handler(handlerRequest)).statusCode mustEqual 500
      }

      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.exception(new IllegalStateException))
        val responseFromBuilder = Await.result(handler.dispatch(request)).build
        responseFromBuilder.getStatusCode() ==== 500
      }
    }
  }
}
