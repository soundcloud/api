package com.soundcloud.publicApiStrangler.support.oauth

sealed abstract class TokenExchangeRequestError(val errorType: String) {
  def reason: String
}

case class MissingClientCredentials() extends TokenExchangeRequestError("missing_client_credentials") {
  def reason: String = "unknown"
}

case class InvalidGrant(requestedGrantType: String) extends TokenExchangeRequestError("invalid_grant") {
  def reason: String = requestedGrantType
}

case class UnparseableRequest(mediaType: Option[String]) extends TokenExchangeRequestError("unparseable_request_body") {
  def reason: String = mediaType.getOrElse("unknown")
}

case class InvalidRequest(reason: String) extends TokenExchangeRequestError("invalid_request")

case class UnsupportedGrantType(requestedGrantType: String)
    extends TokenExchangeRequestError("unsupported_grant_type") {
  def reason: String = requestedGrantType
}
