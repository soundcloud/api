package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest

class TokenExchangeRequestParser(railsLikeParamsParser: RailsLikeParamsParser) {
  def parse(request: HandlerRequest): Either[TokenExchangeRequestError, TokenExchangeRequest] = {
    for {
      params <- readParams(request)
      clientCredential <- readClientCredential(params)
      accessGrant <- readAccessGrant(params)
    } yield TokenExchangeRequest(clientCredential, accessGrant)
  }

  private def readParams(request: HandlerRequest): Either[TokenExchangeRequestError, Map[String, String]] =
    railsLikeParamsParser.parse(request) match {
      case Some(params) => Right(params)
      case _ => Left(UnparseableRequest(request.mediaType))
    }

  private def readClientCredential(params: Map[String, String]): Either[TokenExchangeRequestError, ClientCredential] = {
    params match {
      case ClientCredential(credential) => Right(credential)
      case _ => Left(MissingClientCredentials())
    }
  }

  private def readAccessGrant(params: Map[String, String]): Either[TokenExchangeRequestError, AccessGrant] = {
    params match {
      case AuthorizationCode(authorizationCode) => Right(authorizationCode)
      case ClientCredentialsGrant(credentialsGrant) => Right(credentialsGrant)
      case ResourceOwnerPasswordCredentials(passwordCredentials) => Right(passwordCredentials)
      case RefreshToken(token) => Right(token)
      case _ => Left(UnsupportedGrantType(params.get("grant_type")))
    }
  }
}
