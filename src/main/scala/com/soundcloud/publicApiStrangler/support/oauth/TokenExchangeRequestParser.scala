package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest

class TokenExchangeRequestParser(railsLikeParamsParser: RailsLikeParamsParser) {
  def parse(request: HandlerRequest): Either[TokenExchangeRequestError, TokenExchangeRequest] =
    for {
      params <- readParams(request)
      accessGrant <- readAccessGrant(params)
      clientCredential <- readClientCredential(params)
    } yield TokenExchangeRequest(clientCredential, accessGrant)

  private def readParams(request: HandlerRequest): Either[TokenExchangeRequestError, AccessGrant.Params] =
    railsLikeParamsParser.parse(request) match {
      case Some(params) => Right(AccessGrant.Params.from(params))
      case _ => Left(UnparseableRequest(request.mediaType))
    }

  private def readClientCredential(params: AccessGrant.Params): Either[TokenExchangeRequestError, ClientCredential] =
    ClientCredential.from(params)

  private def readAccessGrant(params: AccessGrant.Params): Either[TokenExchangeRequestError, AccessGrant] =
    params.grantType match {
      case Some(grantType) =>
        grantType match {
          case AuthorizationCodeGrant.Name => AuthorizationCodeGrant.from(params)
          case ResourceOwnerPasswordCredentialsGrant.Name => ResourceOwnerPasswordCredentialsGrant.from(params)
          case RefreshTokenGrant.Name => RefreshTokenGrant.from(params)
          case ClientCredentialsGrant.Name => ClientCredentialsGrant.from(params)
          case _ => Left(UnsupportedGrantType(Some(grantType)))
        }
      case _ => Left(InvalidRequest("missing_grant_type"))
    }
}
