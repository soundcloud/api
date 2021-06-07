package com.soundcloud.publicApiStrangler.service.oauth

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.support.oauth._
import com.twitter.util.Future
import proto.soundcloud.authenticator.{access_grant_exchange => proto}
import proto.AccessGrantExchangeResponse.{Error => AccessGrantExchangeError}

class TokenExchangeService(accessGrantExchangeService: proto.AccessGrantExchangeService) {
  def exchange(exchangeRequest: TokenExchangeRequest): OutcomeF[AccessTokenResponse] = {
    dispatch(exchangeRequest)
      .map(transformResponse)
      .outcomeF
  }

  private def dispatch(request: TokenExchangeRequest): Future[proto.AccessGrantExchangeResponse] = {
    val clientCredentials =
      proto.ClientCredential(request.clientCredential.id, request.clientCredential.secret)

    request.accessGrant match {
      case AuthorizationCodeGrant(code, redirectUri) =>
        accessGrantExchangeService
          .authorizationCodeExchange(proto.AuthorizationCodeGrant(Some(clientCredentials), code, redirectUri))
      case ClientCredentialsGrant(scope) =>
        accessGrantExchangeService
          .clientCredentialExchange(proto.ClientCredentialGrant(Some(clientCredentials), scope.toSeq))
      case RefreshTokenGrant(refreshToken) =>
        accessGrantExchangeService
          .refreshTokenExchange(proto.RefreshTokenGrant(Some(clientCredentials), refreshToken))
      case PasswordGrant(username, password, scope) =>
        accessGrantExchangeService
          .passwordExchange(
            proto.PasswordGrant(
              Some(clientCredentials),
              username,
              password,
              scope.toSeq,
              request.context.remoteIp,
              Some(request.context.userAgent)
            )
          )
    }
  }

  private def transformResponse(resp: proto.AccessGrantExchangeResponse): Outcome[AccessTokenResponse] =
    resp.result match {
      case proto.AccessGrantExchangeResponse.Result.AccessToken(token) =>
        AccessTokenResponse(
          token.accessToken,
          token.expiresInSeconds,
          token.refreshToken,
          token.scope
        ).good
      case proto.AccessGrantExchangeResponse.Result.Error(value) =>
        value match {
          case AccessGrantExchangeError.INVALID_CLIENT => NotAuthorized("invalid_client").bad
          case AccessGrantExchangeError.INVALID_GRANT => NotValid("invalid_grant").bad
          case AccessGrantExchangeError.INVALID_SCOPE => NotAuthorized("invalid_grant").bad
          case proto.AccessGrantExchangeResponse.Error.Unrecognized(unrecognizedValue) =>
            throw new IllegalArgumentException(
              s"Unrecognised response from authenticator proto client: $unrecognizedValue"
            )
        }
      case proto.AccessGrantExchangeResponse.Result.Empty =>
        throw new IllegalArgumentException("Unexpected empty response from authenticator proto client.")
    }
}
