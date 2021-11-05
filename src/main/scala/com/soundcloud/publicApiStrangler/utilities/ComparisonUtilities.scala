package com.soundcloud.publicApiStrangler.utilities

import cats.implicits._
import com.softwaremill.diffx.{Derived, Diff, compare}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.{Counter, Telemetry}
import com.soundcloud.jvmkit.module.util.Urn
import org.joda.time.DateTime

import scala.annotation.nowarn

@nowarn("msg=magnolia: using fallback derivation")
class ComparisonUtilities[T](
    telemetry: Telemetry,
    exceptionCollector: ExceptionCollector
) {
  private val inconsistentResponsesCounter: Counter =
    telemetry.counter(
      "inconsistent_response_total",
      "Count of inconsistent responses between legacy and new path",
      "route",
      "status"
    )

  private val consistentResponsesCounter =
    telemetry.counter(
      "consistent_response_total",
      "Count of consistent responses between legacy and new path",
      "route"
    )

  implicit val urnDiff: Derived[Diff[Urn]] = Diff.derived[Urn]
  implicit val dateTimeDiff: Derived[Diff[DateTime]] = Diff.derived[DateTime]
  implicit val responseDiff = Diff.useEquals[T]

  def compareAndReport(
      endpoint: String,
      oldOutcome: OutcomeF[T],
      newOutcome: OutcomeF[T],
      maybeUserUrn: Option[Urn]
  ): Unit = {

    (oldOutcome, newOutcome)
      .mapN {
        case (oldResponse, newResponse) => {
          val diff = compare[T](oldResponse, newResponse)
          if (!diff.isIdentical) {
            inconsistentResponsesCounter.labels(endpoint, "ok").inc()
            exceptionCollector.addMessage(
              s"[inconsistent-responses]: Non-identical payloads for endpoint [$endpoint]",
              s"Inconsistency in $endpoint response: " + s"${diff.show(true)}" +
                s" for the user with urn: $maybeUserUrn"
            )
          } else {
            consistentResponsesCounter.labels(endpoint).inc()
          }
          newResponse
        }
      }
      .handleErrorWith(error => {
        handleOnlyOneFailed(
          endpoint,
          oldOutcome,
          newOutcome,
          error,
          maybeUserUrn
        )
        handleBothFailed(
          endpoint,
          oldOutcome,
          newOutcome,
          maybeUserUrn
        )
        newOutcome
      })
    ()
  }

  private def handleOnlyOneFailed(
      endpoint: String,
      oldOutcome: OutcomeF[T],
      newOutcome: OutcomeF[T],
      error: ApplicationError,
      maybeUserUrn: Option[Urn]
  ): OutcomeF[T] = {
    // legacy path was successful but not new path
    oldOutcome.onGood(oldResponse => {
      exceptionCollector.addMessage(
        s"[inconsistent-responses]: Only the new path failed for endpoint [${endpoint}]",
        s"Inconsistency in $endpoint response: the legacy path was successful with the " +
          s"following response: $oldResponse, however the new path failed with: $error," +
          s" for the user with urn: $maybeUserUrn"
      )
      inconsistentResponsesCounter.labels(endpoint, "error").inc()
    })

    // new response path was successful but not the legacy one
    newOutcome.onGood(newResponse => {
      exceptionCollector.addMessage(
        s"[inconsistent-responses]: Only the legacy path failed for endpoint [$endpoint]",
        s"Inconsistency in $endpoint response: the new path was successful with the " +
          s"following response: $newResponse, however the legacy path failed with: $error," +
          s" for the user with urn: $maybeUserUrn"
      )
      inconsistentResponsesCounter.labels(endpoint, "error").inc()
    })
  }

  private def handleBothFailed(
      endpoint: String,
      oldOutcome: OutcomeF[T],
      newOutcome: OutcomeF[T],
      maybeUserUrn: Option[Urn]
  ): OutcomeF[T] = {
    oldOutcome.handleErrorWith(oldResponseError => {
      newOutcome.handleErrorWith(newResponseError => {
        if (!oldResponseError.equals(newResponseError)) {
          exceptionCollector.addMessage(
            s"[inconsistent-responses]: Both paths failed for the endpoint [$endpoint]",
            s"Inconsistency in $endpoint response: the new path failed with: $newResponseError, " +
              s"however the legacy path failed with: $oldResponseError for the user with urn: $maybeUserUrn"
          )
          inconsistentResponsesCounter.labels(endpoint, "error").inc()
        } else {
          consistentResponsesCounter.labels(endpoint).inc()
        }
        newOutcome
      })
      newOutcome
    })
  }
}
