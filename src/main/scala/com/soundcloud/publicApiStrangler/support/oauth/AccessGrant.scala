package com.soundcloud.publicApiStrangler.support.oauth

abstract sealed class AccessGrant(val grantType: String)

object AccessGrant {
  type Validation[A] = Either[TokenExchangeRequestError, A]

  case class Params(
      grantType: Option[String],
      clientId: Option[String],
      clientSecret: Option[String],
      code: Option[String],
      redirectUri: Option[String],
      refreshToken: Option[String],
      username: Option[String],
      password: Option[String]
  )

  object Params {
    def from(values: Map[String, String]): Params = {
      def getNonBlank(key: String) = values.get(key).map(_.trim).filter(!_.isEmpty)

      Params(
        getNonBlank("grant_type"),
        getNonBlank("client_id"),
        getNonBlank("client_secret"),
        getNonBlank("code"),
        getNonBlank("redirect_uri"),
        getNonBlank("refresh_token"),
        getNonBlank("username"),
        getNonBlank("password")
      )
    }
  }
}

case class AuthorizationCodeGrant(code: String, redirectUri: String) extends AccessGrant(AuthorizationCodeGrant.Name)

object AuthorizationCodeGrant {
  val Name: String = "authorization_code"

  def from(params: AccessGrant.Params): AccessGrant.Validation[AuthorizationCodeGrant] =
    params match {
      case AccessGrant.Params(Some(Name), _, _, Some(code), Some(redirectUri), _, _, _) =>
        Right(AuthorizationCodeGrant(code, redirectUri))
      case _ => Left(InvalidGrant(params.grantType))
    }
}

case class ClientCredentialsGrant() extends AccessGrant(ClientCredentialsGrant.Name)

object ClientCredentialsGrant {
  val Name: String = "client_credentials"

  def from(params: AccessGrant.Params): AccessGrant.Validation[ClientCredentialsGrant] =
    params match {
      case AccessGrant.Params(Some(Name), _, _, _, _, _, _, _) => Right(ClientCredentialsGrant())
      case _ => Left(InvalidGrant(params.grantType))
    }
}

case class RefreshTokenGrant(refreshToken: String) extends AccessGrant(RefreshTokenGrant.Name)

object RefreshTokenGrant {
  val Name: String = "refresh_token"

  def from(params: AccessGrant.Params): AccessGrant.Validation[RefreshTokenGrant] =
    params match {
      case AccessGrant.Params(Some(Name), _, _, _, _, Some(refreshToken), _, _) =>
        Right(RefreshTokenGrant(refreshToken))
      case _ => Left(InvalidGrant(params.grantType))
    }
}

case class ResourceOwnerPasswordCredentialsGrant(username: String, password: String)
    extends AccessGrant(ResourceOwnerPasswordCredentialsGrant.Name)

object ResourceOwnerPasswordCredentialsGrant {
  val Name: String = "password"

  def from(params: AccessGrant.Params): AccessGrant.Validation[ResourceOwnerPasswordCredentialsGrant] =
    params match {
      case AccessGrant.Params(Some(Name), _, _, _, _, _, Some(username), Some(password)) =>
        Right(ResourceOwnerPasswordCredentialsGrant(username, password))
      case _ => Left(InvalidGrant(params.grantType))
    }
}
