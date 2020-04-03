package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.service.oauth.AuthorizationService
import com.soundcloud.publicApiStrangler.support.oauth.{TokenExchangeRequest, TokenExchangeRequestError}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TokenExchangeHandler(
    mothershipDispatch: Handler,
    telemetry: Telemetry,
    parseRequest: HandlerRequest => Either[TokenExchangeRequestError, TokenExchangeRequest],
    authorizationService: AuthorizationService
) {

  def instrumentedMothershipDispatch(request: HandlerRequest): Future[Response] = {
    parseRequest(request) match {
      case Right(TokenExchangeRequest(credential, accessGrant)) =>
        for {
          response <- mothershipDispatch(request)
          isValid <- authorizationService.validateAccessGrant(credential, accessGrant)
        } yield {
          grantTypeCounter
            .labels(accessGrant.grantType, response.statusCode.toString, isValid.toString)
            .inc()

          response
        }

      case Left(error) =>
        val response = Response(Status.BadRequest)

        requestErrorCounter
          .labels(error.errorType, error.reason, response.statusCode.toString)
          .inc()

        Future.value(response)
    }
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
