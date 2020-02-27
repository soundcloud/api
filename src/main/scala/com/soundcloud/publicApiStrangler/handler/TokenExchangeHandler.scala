package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.support.oauth.{TokenExchangeRequest, TokenExchangeRequestError}
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class TokenExchangeHandler(
    mothershipDispatch: Handler,
    telemetry: Telemetry,
    parseRequest: HandlerRequest => Either[TokenExchangeRequestError, TokenExchangeRequest]
) {

  def instrumentedMothershipDispatch(request: HandlerRequest): Future[Response] = {
    instrument(request)(mothershipDispatch)
  }

  private def instrument(request: HandlerRequest)(handler: Handler): Future[Response] = {
    handler(request).foreach(response =>
      parseRequest(request) match {
        case Right(TokenExchangeRequest(_, accessGrant)) =>
          grantTypeCounter
            .labels(accessGrant.grantType, response.statusCode.toString)
            .inc()
        case Left(error) =>
          requestErrorCounter
            .labels(error.errorType, error.reason, response.statusCode.toString)
            .inc()
      }
    )
  }

  private val grantTypeCounter = telemetry.counter(
    "oauth_token_exchange_grant_type",
    "OAuth 2 Token exchange request grant type.",
    "grant_type",
    "response_status"
  )

  private val requestErrorCounter = telemetry.counter(
    "oauth_token_exchange_error",
    "OAuth 2 Token exchange error.",
    "error",
    "reason",
    "response_status"
  )
}
