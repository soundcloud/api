package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
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
      val handlerRequest = HandlerRequest(AlwaysMatchesPathMatcher, request)
    }

    "returns the response verbatim" >> {
      "for defaultHandling" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        Await.result(handler(handlerRequest)) must be_==(response)
      }

      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        val responseFromHandler = Await.result(handler.dispatch(request))
        responseFromHandler.getStatusCode() ==== response.getStatusCode()
        responseFromHandler.headerMap.get("header1") ==== Some("valueHeader1")
        responseFromHandler.headerMap.get("header2") ==== Some("valueHeader2")
        responseFromHandler.getContentString() ==== "body content"
      }

    }

    "returns 500 for failed requests" >> {
      "for defaultHandling" in new Context {
        mothershipClient(any[Request]) returns (Future.exception(new IllegalStateException))
        Await.result(handler(handlerRequest)).statusCode mustEqual 500
      }

      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.exception(new IllegalStateException))
        val responseFromHandler = Await.result(handler.dispatch(request))
        responseFromHandler.getStatusCode() ==== 500
      }
    }
  }
}
