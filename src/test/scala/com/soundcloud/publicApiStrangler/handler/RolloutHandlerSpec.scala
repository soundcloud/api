package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class RolloutHandlerSpec extends Specification {
  trait Context extends Scope {
    val rolloutResult: Boolean
    val defaultResponse: Response = ResponseBuilder.ok(body = "default")
    val alternativeResponse: Response = ResponseBuilder.ok(body = "alternative")

    lazy val underlyingChoice: () => Future[Boolean] = () => Future(rolloutResult)
    lazy val choice: () => Future[Boolean] = underlyingChoice

    lazy val handle: Handler =
      new RolloutHandler(
        choice,
        _ => Future(defaultResponse),
        _ => Future(alternativeResponse)
      ).handle _
  }

  "chooses the default when the rollout is disabled" in new Context {
    val rolloutResult = false

    Await.result(handle(HandlerRequest())) ==== defaultResponse
  }

  "chooses the alternative when the rollout is enabled" in new Context {
    val rolloutResult = true

    Await.result(handle(HandlerRequest())) ==== alternativeResponse
  }

  "RolloutHandler.InstrumentedPredicate" >> {
    trait InstrumentedChoiceContext extends Scope {
      val rolloutResult: Boolean
      val defaultResponse: Response = ResponseBuilder.ok(body = "default")
      val alternativeResponse: Response =
        ResponseBuilder.ok(body = "alternative")

      val telemetry = Telemetry.createIsolatedInstance
      val counter = telemetry.counter("name_total", "docString", "static", "choice")
      val instrumentedChoice: () => Future[Boolean] =
        new RolloutHandler.InstrumentedPredicate(
          () => Future(rolloutResult),
          counter,
          Seq("static")
        )
    }

    "instruments the negative choice" in new InstrumentedChoiceContext {
      val rolloutResult = false

      Await.result(instrumentedChoice())

      counter.labels("static", "false").get ==== 1.0
    }

    "instruments the positive choice" in new InstrumentedChoiceContext {
      val rolloutResult = true

      Await.result(instrumentedChoice())

      counter.labels("static", "true").get ==== 1.0
    }

    "integration" >> {
      trait IntegrationContext extends Context {
        val telemetry = Telemetry.createIsolatedInstance
        val counter = telemetry.counter("name_total", "docString", "choice")

        override lazy val choice: () => Future[Boolean] =
          new RolloutHandler.InstrumentedPredicate(
            underlyingChoice,
            counter
          )
      }

      "instruments the negative choice" in new IntegrationContext {
        val rolloutResult = false

        Await.result(handle(HandlerRequest()))

        counter.labels("false").get ==== 1.0
      }

      "instruments the positive choice" in new IntegrationContext {
        val rolloutResult = true

        Await.result(handle(HandlerRequest()))

        counter.labels("true").get ==== 1.0
      }
    }
  }
}
