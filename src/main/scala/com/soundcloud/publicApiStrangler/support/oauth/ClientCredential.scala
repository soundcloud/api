package com.soundcloud.publicApiStrangler.support.oauth

case class ClientCredential(id: String, secret: String)

object ClientCredential {
  def unapply(values: Map[String, String]): Option[ClientCredential] = {
    val clientId = values.get("client_id")
    val clientSecret = values.get("client_secret")

    (clientId, clientSecret) match {
      case (Some(id), Some(secret)) => Some(ClientCredential(id, secret))
      case _ => None
    }
  }
}
