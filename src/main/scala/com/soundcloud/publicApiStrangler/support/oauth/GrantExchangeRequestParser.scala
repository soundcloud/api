package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest

class GrantExchangeRequestParser(railsLikeParamsParser: RailsLikeParamsParser) {
  def parse(request: HandlerRequest): Either[GrantExchangeRequestError, GrantExchangeRequest] =
    for {
      params <- railsLikeParamsParser.parse(request).toRight(UnparseableRequest(request.mediaType))
      paramsWithLastValue = params.map(tuple => (tuple._1, tuple._2.last))
      accessGrant <- readAccessGrant(paramsWithLastValue)
      clientCredential <- readClientCredential(paramsWithLastValue)
      context <- readContext(request)
    } yield GrantExchangeRequest(clientCredential, accessGrant, context)

  private def getNonBlank(values: Map[String, String], key: String) =
    values.get(key).filter(_.trim.nonEmpty)

  private def readClientCredential(params: Map[String, String]): Either[GrantExchangeRequestError, ClientCredential] = {
    val credOpt = for {
      clientId <- getNonBlank(params, "client_id")
      clientSecret <- getNonBlank(params, "client_secret")
    } yield ClientCredential(clientId, clientSecret)
    credOpt.toRight(MissingClientCredentials())
  }

  private def readAccessGrant(params: Map[String, String]): Either[GrantExchangeRequestError, AccessGrant] =
    getNonBlank(params, "grant_type") match {
      case Some(AuthorizationCodeGrant.Name) =>
        val grantOpt = for {
          code <- getNonBlank(params, "code")
          redirectUri <- getNonBlank(params, "redirect_uri")
        } yield AuthorizationCodeGrant(code, redirectUri)
        grantOpt.toRight(InvalidGrant(AuthorizationCodeGrant.Name))

      case Some(RefreshTokenGrant.Name) =>
        val grantOpt = for {
          refreshToken <- getNonBlank(params, "refresh_token")
        } yield RefreshTokenGrant(refreshToken)
        grantOpt.toRight(InvalidGrant(RefreshTokenGrant.Name))

      case Some(ClientCredentialsGrant.Name) =>
        Right(ClientCredentialsGrant(getScope(params)))

      case Some(other) => Left(UnsupportedGrantType(other))

      case None =>
        Left(InvalidRequest("missing_grant_type"))
    }

  private def getScope(values: Map[String, String]): Set[String] = {
    getNonBlank(values, "scope")
      .getOrElse("")
      .split(" ")
      .filter(_.nonEmpty)
      .toSet
  }

  private def readContext(request: HandlerRequest): Either[GrantExchangeRequestError, RequestContext] =
    (request.remoteIp(), request.headerMap.get("User-Agent").getOrElse("")) match {
      case (Some(remoteIp), userAgent) => Right(RequestContext(remoteIp, userAgent))
      case _ => Left(InvalidRequest("remote_ip_missing"))
    }
}
