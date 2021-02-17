package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import org.specs2.matcher.Matcher

class DispatchToMothershipHandlerSpec extends UnitSpecification {
  "dispatches authenticated requests to the mothership" >> {
    trait Context extends Scope {
      val mothershipClient = mock[Service[Request, Response]]
      val session = loggedInSession(Urn("soundcloud", "users", "1"))
      val userAuthentication = new FakeUserAuthentication(session)

      val handler = new DispatchToMothershipHandler(userAuthentication, mothershipClient)

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

    "useInternalHeaders header" >> {
      trait UseInternalHeadersContext extends Context {
        val sessionClientUrn = Urn("soundcloud", "applications", "123")
        override val session = loggedInSession(Urn("soundcloud", "users", "1")).copy(agent = Some(sessionClientUrn))
        override val userAuthentication = new FakeUserAuthentication(session)

        mothershipClient(any[Request]) returns (Future.value(response))
        def responseFromHandler: Response = Await.result(handler.dispatch(handlerRequest))

        def useInternalHeadersIsPresent: Matcher[Request] = {
          (_: Request).headerMap.keySet must contain("X-oauth-use-internal-headers")
        }
      }

      "is added for client requests on useInternalHeadersClients list" in new UseInternalHeadersContext {
        val userInternalHeadersClients = Set(sessionClientUrn)
        override val handler =
          new DispatchToMothershipHandler(userAuthentication, mothershipClient, userInternalHeadersClients)

        responseFromHandler.status ==== response.status
        there was one(mothershipClient).apply(useInternalHeadersIsPresent)
      }

      "is not added for client not list" in new UseInternalHeadersContext {
        val userInternalHeadersClients = Set(Urn("soundcloud", "applications", "999"))
        override val handler =
          new DispatchToMothershipHandler(userAuthentication, mothershipClient, userInternalHeadersClients)

        responseFromHandler.status ==== response.status
        there was one(mothershipClient).apply(any[Request])
        there was no(mothershipClient).apply(useInternalHeadersIsPresent)
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

  "dispatches unauthenticated requests to the mothership" >> {
    trait Context extends Scope {
      val mothershipClient = mock[Service[Request, Response]]
      val session = loggedInSession(Urn("soundcloud", "users", "1"))
      val userAuthentication = new FakeUserAuthentication(session)

      val handler = new DispatchToMothershipHandler(userAuthentication, mothershipClient)

      val response = Response(Status.Ok)
      response.headerMap.set("header1", "valueHeader1").set("header2", "valueHeader2")
      response.contentString = "body content"

      val handlerRequest = HandlerRequest()
    }

    "returns the response verbatim" >> {
      "for dispatch method" in new Context {
        mothershipClient(any[Request]) returns (Future.value(response))
        val responseFromHandler = Await.result(handler.dispatchUnauthenticated(handlerRequest))

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
        val responseFromHandler = Await.result(handler.dispatchUnauthenticated(handlerRequest))
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
          Await.result(handler.dispatchUnauthenticated(handlerRequest))
        } catch {
          case (e: Throwable) => e
        }) ==== fatalException
      }
    }
  }
}
