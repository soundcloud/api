package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.AuthorizationService
import com.soundcloud.publicApiStrangler.support.oauth.{
  InvalidGrant,
  InvalidRequest,
  MissingClientCredentials,
  TokenExchangeRequest,
  TokenExchangeRequestError,
  UnparseableRequest,
  UnsupportedGrantType
}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

class TokenExchangeHandler(
    mothershipDispatch: Handler,
    telemetry: Telemetry,
    parseRequest: HandlerRequest => Either[TokenExchangeRequestError, TokenExchangeRequest],
    authorizationService: AuthorizationService
) {

  def instrumentedMothershipDispatch(request: HandlerRequest): Future[Response] =
    parseRequest(request) match {
      case Right(TokenExchangeRequest(credential, accessGrant)) =>
        for {
          (response, isValid) <- Future.join(
            mothershipDispatch(request),
            authorizationService.validateAccessGrant(credential, accessGrant)
          )
        } yield {
          grantTypeCounter
            .labels(accessGrant.grantType, response.statusCode.toString, isValid.toString)
            .inc()

          response
        }
      case Left(error) =>
        val response =
          JsonResponseBuilder(Status.BadRequest, Json.stringify(Json.obj("error_code" -> errorCode(error))))

        requestErrorCounter
          .labels(error.errorType, error.reason, response.status.code.toString)
          .inc()

        Future.value(response.build)
    }

  private def errorCode(error: TokenExchangeRequestError) =
    error match {
      case InvalidRequest(_) | UnparseableRequest(_) => "invalid_request"
      case UnsupportedGrantType(_) => "unsupported_grant_type"
      case InvalidGrant(_) | MissingClientCredentials() => "invalid_grant"
    }

  private val grantTypeCounter = telemetry.counter(
    "oauth_token_exchange_grant_type",
    "OAuth 2 Token exchange request grant type.",
    "grant_type",
    "response_status",
    "is_valid"
  )

  private val requestErrorCounter = telemetry.counter(
    "oauth_token_exchange_error",
    "OAuth 2 Token exchange error.",
    "error",
    "reason",
    "response_status"
  )
}
