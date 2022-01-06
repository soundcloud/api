package com.soundcloud.apipublic.support.oauth

abstract sealed class AccessGrant(val grantType: String)

object AccessGrant {
  type Validation[A] = Either[GrantExchangeRequestError, A]
}

case class AuthorizationCodeGrant(code: String, redirectUri: String) extends AccessGrant(AuthorizationCodeGrant.Name)

object AuthorizationCodeGrant {
  val Name: String = "authorization_code"
}

case class ClientCredentialsGrant(scope: Set[String]) extends AccessGrant(ClientCredentialsGrant.Name)

object ClientCredentialsGrant {
  val Name: String = "client_credentials"
}

case class RefreshTokenGrant(refreshToken: String) extends AccessGrant(RefreshTokenGrant.Name)

object RefreshTokenGrant {
  val Name: String = "refresh_token"
}
