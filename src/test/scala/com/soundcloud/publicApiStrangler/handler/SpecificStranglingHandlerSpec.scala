package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.AuthorizationHeaders.ScHeaders
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{HeaderMap, Method, Request, Response}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.verify

class SpecificStranglingHandlerSpec extends UnitSpecification {
  trait TestHandler {
    def handle(handlerRequest: HandlerRequest): Future[Response]
  }

  trait Context extends Scope {
    val pathPatternsToDispatch = List(
      """/announcements""".r,
      """/e1/playlists/\d+/domain-lockings""".r
    )
    val officialApps = List(Urn("soundcloud", "applications", "124"))
    val agentUrn = Urn("soundcloud", "applications", "124")

    val request = mock[HandlerRequest]
    val innerRequest = mock[Request]
    request.request returns innerRequest
    innerRequest.method returns Method("GET")
    request.headerMap returns HeaderMap(ScHeaders.AGENT.header -> agentUrn.toString)

    // we can't mock the handler function directly.
    // Thus we define a mock handler object and hand it's handle function to the object under test
    val testHandler = mock[TestHandler]
    testHandler.handle(request) returns Future(mock[Response])

    val telemetry = Telemetry.createIsolatedInstance
    val counter =
      telemetry.counter("fallthrough_strangled_by", "testing counter", "method", "path_pattern", "agent_urn")

    val handler = new SpecificStranglingHandler(testHandler.handle, pathPatternsToDispatch, officialApps, counter)
  }

  "dispatching" >> {
    "it dispatches requests to the handler" in new Context {
      Await.result(handler.apply(request))
      verify(testHandler).handle(request)
    }
  }

  "path pattern handling" >> {
    trait KnownUrlContext extends Context {
      innerRequest.path returns "/announcements"
    }

    trait UnknownUrlContext extends Context {
      innerRequest.path returns "/unknown/endpoint"
    }

    "it increments the counter with the path pattern, if it is recognised" in new KnownUrlContext {
      Await.result(handler.apply(request))
      val count = telemetry.getSampleValue(
        "fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn"),
        Array("GET", "/announcements", "soundcloud:applications:124")
      )
      count ==== Some(1.0)
    }

    "it increments the counter with 'UNKNOWN' if it is not recognised" in new UnknownUrlContext {
      Await.result(handler.apply(request))
      val count = telemetry.getSampleValue(
        "fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn"),
        Array("GET", "UNKNOWN", "soundcloud:applications:124")
      )
      count ==== Some(1.0)
    }
  }

  "agent URN handling" >> {
    trait KnownAgentContext extends Context {
      innerRequest.path returns "/announcements"
    }

    trait UnknownAgentContext extends Context {
      request.headerMap returns HeaderMap(ScHeaders.AGENT.header -> "soundcloud:applications:99999")
      innerRequest.path returns "/announcements"
    }

    "it increments the counter with the agent URN, if it is recognised" in new KnownAgentContext {
      Await.result(handler.apply(request))

      val count = telemetry.getSampleValue(
        "fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn"),
        Array("GET", "/announcements", "soundcloud:applications:124")
      )
      count ==== Some(1.0)
    }

    "it increments the counter with 'soundcloud:applications:external, if it is not recognised" in new UnknownAgentContext {
      Await.result(handler.apply(request))
      val count = telemetry.getSampleValue(
        "fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn"),
        Array("GET", "/announcements", "soundcloud:applications:external")
      )
      count ==== Some(1.0)
    }
  }
}
