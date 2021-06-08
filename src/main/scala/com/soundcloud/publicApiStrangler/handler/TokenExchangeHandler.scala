package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.{AuthorizationService, TokenExchangeService}
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.oauth._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{JsString, Json}

class TokenExchangeHandler(
    mothershipDispatch: Handler,
    telemetry: Telemetry,
    parseRequest: HandlerRequest => Either[TokenExchangeRequestError, TokenExchangeRequest],
    authorizationService: AuthorizationService,
    tokenExchangeService: TokenExchangeService,
    authenticatorClientIdList: Set[String]
) extends Handler {

  def apply(request: HandlerRequest): Future[Response] = {
    parseRequest(request) match {
      case Right(request) if authenticatorClientIdList.contains(request.clientCredential.id) =>
        dispatchToAuthenticator(request)
      case parseResult => instrumentedMothershipDispatch(request, parseResult)
    }
  }

  private def dispatchToAuthenticator(request: TokenExchangeRequest): Future[Response] =
    tokenExchangeService.exchange(request).value.map {
      case Good(accessToken) =>
        incrementGrantExchangeCounter(request.accessGrant, Status.Ok)
        ResponseBuilder.ok(Json.stringify(Json.toJson(accessToken)))

      case Bad(NotValid(reason :: _)) =>
        incrementGrantExchangeCounter(request.accessGrant, Status.BadRequest, reason)
        buildErrorResponse(Status.BadRequest, reason)
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

  private def instrumentedMothershipDispatch(
      request: HandlerRequest,
      parseResult: Either[TokenExchangeRequestError, TokenExchangeRequest]
  ): Future[Response] =
    parseResult match {
      case Right(TokenExchangeRequest(credential, accessGrant, context)) =>
        for {
          (response, isValid) <- Future.join(
            mothershipDispatch(request),
            authorizationService.validateAccessGrant(credential, accessGrant, context)
          )
        } yield {
          grantTypeCounter
            .labels(accessGrant.grantType, response.statusCode.toString, isValid.toString)
            .inc()

          response
        }
      case Left(error) =>
        val response = ErrorResponse(
          Status.BadRequest,
          errorCode(error),
          Some(Map("error_code" -> JsString(errorCode(error)))) // backwards compatibility
        )

        requestErrorCounter
          .labels(error.errorType, error.reason, response.status.code.toString)
          .inc()

        Future.value(response)
    }

  private def errorCode(error: TokenExchangeRequestError) =
    error match {
      case InvalidRequest(_) | UnparseableRequest(_) => "invalid_request"
      case UnsupportedGrantType(_) => "unsupported_grant_type"
      case InvalidGrant(_) | MissingClientCredentials() => "invalid_grant"
    }

  private val grantTypeCounter = telemetry.counter(
    "oauth_token_exchange_grant_type_total",
    "OAuth 2 Token exchange request grant type.",
    "grant_type",
    "response_status",
    "is_valid"
  )

  private val requestErrorCounter = telemetry.counter(
    "oauth_token_exchange_error_total",
    "OAuth 2 Token exchange error.",
    "error",
    "reason",
    "response_status"
  )

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
}
