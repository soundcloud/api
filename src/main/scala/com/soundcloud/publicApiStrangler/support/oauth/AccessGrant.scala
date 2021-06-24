package com.soundcloud.publicApiStrangler.support.oauth

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

case class PasswordGrant(username: String, password: String, scope: Set[String]) extends AccessGrant(PasswordGrant.Name)

object PasswordGrant {
  val Name: String = "password"
}
