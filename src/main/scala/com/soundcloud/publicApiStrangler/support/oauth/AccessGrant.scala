package com.soundcloud.publicApiStrangler.support.oauth

sealed class AccessGrant(val grantType: String)

object AccessGrant {
  val authorizationCode: String = "authorization_code"
  val clientCredentials: String = "client_credentials"
  val resourceOwnerPassword: String = "password"
  val refreshToken: String = "refresh_token"
}

case class AuthorizationCode(code: String, redirectUri: String) extends AccessGrant(AccessGrant.authorizationCode)

object AuthorizationCode {
  def unapply(values: Map[String, String]): Option[AuthorizationCode] = {
    val grantType = values.get("grant_type")
    val code = values.get("code")
    val redirectURI = values.get("redirect_uri")

    (grantType, code, redirectURI) match {
      case (Some(AccessGrant.authorizationCode), Some(c), Some(r)) => Some(AuthorizationCode(c, r))
      case _ => None
    }
  }
}

case class ClientCredentialsGrant() extends AccessGrant(AccessGrant.clientCredentials)

object ClientCredentialsGrant {
  def unapply(values: Map[String, String]): Option[ClientCredentialsGrant] = {
    val grantType = values.get("grant_type")

    grantType match {
      case Some(AccessGrant.clientCredentials) => Some(ClientCredentialsGrant())
      case _ => None
    }
  }
}

case class RefreshToken(refreshToken: String) extends AccessGrant(AccessGrant.refreshToken)

object RefreshToken {
  def unapply(values: Map[String, String]): Option[RefreshToken] = {
    val grantType = values.get("grant_type")
    val refreshToken = values.get("refresh_token")

    (grantType, refreshToken) match {
      case (Some(AccessGrant.refreshToken), Some(r)) => Some(RefreshToken(r))
      case _ => None
    }
  }
}

case class ResourceOwnerPasswordCredentials(username: String, password: String)
    extends AccessGrant(AccessGrant.resourceOwnerPassword)

object ResourceOwnerPasswordCredentials {
  def unapply(values: Map[String, String]): Option[ResourceOwnerPasswordCredentials] = {
    val grantType = values.get("grant_type")
    val username = values.get("username")
    val password = values.get("password")

    (grantType, username, password) match {
      case (Some(AccessGrant.resourceOwnerPassword), Some(u), Some(p)) => Some(ResourceOwnerPasswordCredentials(u, p))
      case _ => None
    }
  }
}
