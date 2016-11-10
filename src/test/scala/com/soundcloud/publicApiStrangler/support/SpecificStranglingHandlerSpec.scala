package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.Request
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, HttpHandler}
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.http.{Method, Response}
import com.twitter.util.{Await, Future}
import io.prometheus.client.CollectorRegistry
import org.mockito.Mockito.verify

class SpecificStranglingHandlerSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[HttpHandler]

    val pathPatternsToDispatch = List(
      """/announcements""".r,
      """/search/sounds""".r,
      """/search/sets""".r,
      """/e1/playlists/\d+/domain-lockings""".r
    )
    val officialApps = List(Urn("soundcloud:applications:124"))

    val request = mock[HandlerRequest]
    val innerRequest = mock[Request]
    request.method returns Method("GET")
    request.request returns innerRequest

    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(new InMemoryConfig, collectorRegistry)
    val counter = telemetry.counter("fallthrough_strangled_by", "testing counter", "method", "path_pattern", "agent_urn")

    next.defaultHandling(request) returns Future(mock[Response])
    val handler = new SpecificStranglingHandler(next, pathPatternsToDispatch, officialApps, counter)
  }

  "dispatching" >> {
    "it dispatches requests to the handler" in new Context {
      val userSession = new UserSessionBuilder().setAgent(Urn("soundcloud:applications:124")).build()
      request.userSession returns userSession
      Await.result(handler.apply(request))
      verify(next).defaultHandling(request)
    }
  }

  "path pattern handling" >> {

    trait KnownUrlContext extends Context {
      val agentUrn = Urn("soundcloud:applications:124")
      innerRequest.path returns "/search/sounds"
      request.userSession returns new UserSessionBuilder().setAgent(agentUrn).build()
    }

    trait UnknownUrlContext extends Context {
      val agentUrn = Urn("soundcloud:applications:124")
      innerRequest.path returns "/unknown/endpoint"
      request.userSession returns new UserSessionBuilder().setAgent(agentUrn).build()
    }

    "it increments the counter with the path pattern, if it is recognised" in new KnownUrlContext {
      Await.result(handler.apply(request))
      val count = collectorRegistry.getSampleValue("fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn", "system"),
        Array("GET", "/search/sounds", "soundcloud:applications:124", "TEST-APP"))
      count ==== 1.0
    }

    "it increments the counter with 'UNKNOWN' if it is not recognised" in new UnknownUrlContext {
      Await.result(handler.apply(request))
      val count = collectorRegistry.getSampleValue("fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn", "system"),
        Array("GET", "UNKNOWN", "soundcloud:applications:124", "TEST-APP"))
      count ==== 1.0
    }
  }

  "agent URN handling" >> {

    trait KnownAgentContext extends Context {
      val agentUrn = Urn("soundcloud:applications:124")
      innerRequest.path returns "/search/sounds"
      request.userSession returns new UserSessionBuilder().setAgent(agentUrn).build()
    }

    trait UnknownAgentContext extends Context {
      val agentUrn = Urn("soundcloud:applications:99999")
      innerRequest.path returns "/search/sounds"
      request.userSession returns new UserSessionBuilder().setAgent(agentUrn).build()
    }

    "it increments the counter with the agent URN, if it is recognised" in new KnownAgentContext {
      Await.result(handler.apply(request))
      val count = collectorRegistry.getSampleValue("fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn", "system"),
        Array("GET", "/search/sounds", "soundcloud:applications:124", "TEST-APP"))
      count ==== 1.0
    }

    "it increments the counter with 'soundcloud:applications:external, if it is not recognised" in new UnknownAgentContext {
      Await.result(handler.apply(request))
      val count = collectorRegistry.getSampleValue("fallthrough_strangled_by",
        Array("method", "path_pattern", "agent_urn", "system"),
        Array("GET", "/search/sounds", "soundcloud:applications:external", "TEST-APP"))
      count ==== 1.0
    }
  }
}
