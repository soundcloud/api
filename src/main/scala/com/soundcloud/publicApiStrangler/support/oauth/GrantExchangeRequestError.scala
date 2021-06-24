package com.soundcloud.publicApiStrangler.support.oauth

sealed abstract class GrantExchangeRequestError(val errorType: String) {
  def reason: String
}

case class MissingClientCredentials() extends GrantExchangeRequestError("missing_client_credentials") {
  def reason: String = "unknown"
}

case class InvalidGrant(requestedGrantType: String) extends GrantExchangeRequestError("invalid_grant") {
  def reason: String = requestedGrantType
}

case class UnparseableRequest(mediaType: Option[String]) extends GrantExchangeRequestError("unparseable_request_body") {
  def reason: String = mediaType.getOrElse("unknown")
}

case class InvalidRequest(reason: String) extends GrantExchangeRequestError("invalid_request")

case class UnsupportedGrantType(requestedGrantType: String)
    extends GrantExchangeRequestError("unsupported_grant_type") {
  def reason: String = requestedGrantType
}
