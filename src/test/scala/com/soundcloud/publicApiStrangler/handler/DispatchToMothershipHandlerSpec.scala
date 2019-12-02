package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
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
      response.headerMap.set("header1", "valueHeader1").set("header2", "valueHeader2")
      response.contentString = "body content"

      val handlerRequest = HandlerRequest()
    }

    "returns the response verbatim" >> {
      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        val responseFromHandler = Await.result(handler.dispatch(handlerRequest))

        responseFromHandler.status ==== response.status
        responseFromHandler.headerMap.get("header1") ==== Some("valueHeader1")
        responseFromHandler.headerMap.get("header2") ==== Some("valueHeader2")
        responseFromHandler.getContentString() ==== "body content"
      }
    }

    "returns 500 for non-fatal exceptions" >> {
      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns
          (Future.exception(
            new IllegalStateException
          ))
        val responseFromHandler = Await.result(handler.dispatch(handlerRequest))
        responseFromHandler.status ==== Status.InternalServerError
      }
    }

    "does not handle fatal exceptions" >> {
      "for dispatch method" in new Context {
        private val fatalException = new ThreadDeath
        mothershipClient(any[Request]) returns
          (Future.exception(
            fatalException
          ))

        (try {
          Await.result(handler.dispatch(handlerRequest))
        } catch {
          case (e: Throwable) => e
        }) ==== fatalException
      }
    }
  }
}
