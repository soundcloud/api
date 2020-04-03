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
      case AuthorizationCodeGrant(authorizationCode) => Right(authorizationCode)
      case ClientCredentialsGrant(credentialsGrant) => Right(credentialsGrant)
      case ResourceOwnerPasswordCredentialsGrant(passwordCredentials) => Right(passwordCredentials)
      case RefreshTokenGrant(token) => Right(token)
      case _ => Left(UnsupportedGrantType(params.get("grant_type").map(normalizeUnsupportedGrantType)))
    }
  }

  // We used to support grant type extensions for token exchange via social networks:
  // https://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-4.5
  //
  // This capability has been removed from the public API as part of MRR-304 in:
  // https://github.com/soundcloud/soundcloud/commit/d21cd3659915aab82a683e077b08a5061b790c42
  private def normalizeUnsupportedGrantType(grantType: String): String = {
    grantType match {
      case grant if grant contains "urn:soundcloud:oauth2:grant-type:facebook" => "facebook"
      case grant if grant contains "urn:soundcloud:oauth2:grant-type:google_plus" => "google_plus"
      case grant => grant
    }
  }
}
