package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.GrantExchangeService
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.oauth._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{JsString, Json}

class OauthGrantExchangeHandler(
    telemetry: Telemetry,
    parseRequest: HandlerRequest => Either[GrantExchangeRequestError, GrantExchangeRequest],
    grantExchangeService: GrantExchangeService
) extends Handler {

  def apply(request: HandlerRequest): Future[Response] = {
    parseRequest(request) match {
      case Right(parsedRequest) => dispatchToAuthenticator(parsedRequest)
      case Left(error) =>
        incrementGrantExchangeCounterBadRequest(error)
        buildBadRequestResponse(error)
    }
  }

  // repost app urn soundcloud:applications:314153
  private val repostClientIds =
    Set("1jtbGRxaCld7OEmR7TVYeJy0qaFQqnSf", "RcTym36UFfGVYMkMND74sakJwAw498ME", "SZa63XTt7C8KdgCVBFGoPGJVQojLS4KN")

  private def dispatchToAuthenticator(request: GrantExchangeRequest): Future[Response] =
    grantExchangeService.exchange(request).map {
      case Good(accessToken) =>
        incrementGrantExchangeCounter(request.accessGrant, Status.Ok)

        if (repostClientIds.contains(request.clientCredential.id)) {
          ResponseBuilder.ok(Json.stringify(Json.toJson(accessToken)))
        } else {
          JsonResponseBuilder.ok(Json.stringify(Json.toJson(accessToken)))
        }
      case Bad(NotValid(reason :: _)) =>
        incrementGrantExchangeCounter(request.accessGrant, Status.Unauthorized, reason)
        buildErrorResponse(Status.Unauthorized, reason)
      case Bad(NotAuthorized(reason)) =>
        incrementGrantExchangeCounter(request.accessGrant, Status.Unauthorized, reason)
        buildErrorResponse(Status.Unauthorized, reason)
      case Bad(_) =>
        incrementGrantExchangeCounter(request.accessGrant, Status.BadRequest, "this_should_not_happen")
        buildErrorResponse(Status.BadRequest, "")
    }

  private def buildErrorResponse(status: Status, reason: String) = ErrorResponse(
    status,
    reason,
    Some(Map("error_code" -> JsString(reason)))
  )

  private def buildBadRequestResponse(error: GrantExchangeRequestError): Future[Response] = {
    val response = ErrorResponse(
      Status.BadRequest,
      errorCode(error),
      Some(Map("error_code" -> JsString(errorCode(error)))) // backwards compatibility
    )

    Future.value(response)
  }

  private def errorCode(error: GrantExchangeRequestError) =
    error match {
      case InvalidRequest(_) | UnparseableRequest(_) => "invalid_request"
      case UnsupportedGrantType(_) => "unsupported_grant_type"
      case InvalidGrant(_) | MissingClientCredentials() => "invalid_grant"
    }

  private val grantExchangeCounter = telemetry.counter(
    "oauth_grant_exchange_total",
    "Counter for oauth grant exchanges and results",
    "grant_type",
    "status",
    "reason"
  )

  private def incrementGrantExchangeCounter(accessGrant: AccessGrant, status: Status, reason: String = ""): Unit = {
    grantExchangeCounter.labels(accessGrant.grantType, status.code.toString, reason).inc()
  }

  private def incrementGrantExchangeCounterBadRequest(error: GrantExchangeRequestError): Unit = {
    grantExchangeCounter.labels("unknown", "400", error.errorType).inc()
  }
}
