package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.AuthorizationService
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.oauth._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.JsString

class TokenExchangeHandler(
    mothershipDispatch: Handler,
    telemetry: Telemetry,
    parseRequest: HandlerRequest => Either[TokenExchangeRequestError, TokenExchangeRequest],
    authorizationService: AuthorizationService
) {

  def instrumentedMothershipDispatch(request: HandlerRequest): Future[Response] =
    parseRequest(request) match {
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
}
