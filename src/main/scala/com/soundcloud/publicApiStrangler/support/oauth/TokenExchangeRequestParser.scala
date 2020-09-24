package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest

class TokenExchangeRequestParser(railsLikeParamsParser: RailsLikeParamsParser) {
  def parse(request: HandlerRequest): Either[TokenExchangeRequestError, TokenExchangeRequest] =
    for {
      params <- railsLikeParamsParser.parse(request).toRight(UnparseableRequest(request.mediaType))
      accessGrant <- readAccessGrant(params)
      clientCredential <- readClientCredential(params)
    } yield TokenExchangeRequest(clientCredential, accessGrant)

  private def getNonBlank(values: Map[String, String], key: String) =
    values.get(key).filter(!_.trim.isEmpty)

  private def readClientCredential(params: Map[String, String]): Either[TokenExchangeRequestError, ClientCredential] = {
    val credOpt = for {
      clientId <- getNonBlank(params, "client_id")
      clientSecret <- getNonBlank(params, "client_secret")
    } yield ClientCredential(clientId, clientSecret)
    credOpt.toRight(MissingClientCredentials())
  }

  private def readAccessGrant(params: Map[String, String]): Either[TokenExchangeRequestError, AccessGrant] =
    getNonBlank(params, "grant_type") match {
      case Some(AuthorizationCodeGrant.Name) =>
        val grantOpt = for {
          code <- getNonBlank(params, "code")
          redirectUri <- getNonBlank(params, "redirect_uri")
        } yield AuthorizationCodeGrant(code, redirectUri)
        grantOpt.toRight(InvalidGrant(AuthorizationCodeGrant.Name))

      case Some(ResourceOwnerPasswordCredentialsGrant.Name) =>
        val grantOpt = for {
          username <- getNonBlank(params, "username")
          password <- getNonBlank(params, "password")
        } yield ResourceOwnerPasswordCredentialsGrant(username, password)
        grantOpt.toRight(InvalidGrant(ResourceOwnerPasswordCredentialsGrant.Name))

      case Some(RefreshTokenGrant.Name) =>
        val grantOpt = for {
          refreshToken <- getNonBlank(params, "refresh_token")
        } yield RefreshTokenGrant(refreshToken)
        grantOpt.toRight(InvalidGrant(RefreshTokenGrant.Name))

      case Some(ClientCredentialsGrant.Name) =>
        Right(ClientCredentialsGrant())

      case Some(other) => Left(UnsupportedGrantType(other))

      case None =>
        Left(InvalidRequest("missing_grant_type"))
    }
}
