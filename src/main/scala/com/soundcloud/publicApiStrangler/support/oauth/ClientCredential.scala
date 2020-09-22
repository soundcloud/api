package com.soundcloud.publicApiStrangler.support.oauth

case class ClientCredential(id: String, secret: String)

object ClientCredential {
  def from(params: AccessGrant.Params): Either[TokenExchangeRequestError, ClientCredential] =
    params match {
      case AccessGrant.Params(_, Some(clientId), Some(clientSecret), _, _, _, _, _) =>
        Right(ClientCredential(clientId, clientSecret))
      case _ => Left(MissingClientCredentials())
    }
}
