package com.soundcloud.publicApiStrangler.service.oauth

import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.publicApiStrangler.support.oauth._
import com.twitter.util.Future
import proto.soundcloud.authenticator.{oauth => proto}

import scala.util.control.NonFatal

class AuthorizationService(service: proto.AuthorizationService, exceptionCollector: ExceptionCollector) {
  private val validationFallbackResponse = proto.ValidateAccessGrantResponse(false)

  def validateAccessGrant(clientCredential: ClientCredential, accessGrant: AccessGrant): Future[Boolean] = {
    val credential = proto.ClientCredential(
      clientId = clientCredential.id,
      clientSecret = clientCredential.secret
    )

    val grant = (proto.AccessGrant(clientCredential = Some(credential)), accessGrant) match {
      case (grant, a: AuthorizationCodeGrant) =>
        val authorizationCodeGrant = proto.AuthorizationCodeGrant(
          code = a.code,
          redirectUri = a.redirectUri
        )

        grant.withAuthorizationCodeGrant(authorizationCodeGrant)
      case (grant, _: ClientCredentialsGrant) =>
        grant.withClientCredentialsGrant(proto.ClientCredentialsGrant())
      case (grant, r: RefreshTokenGrant) =>
        grant.withRefreshTokenGrant(proto.RefreshTokenGrant(r.refreshToken))
      case (grant, r: ResourceOwnerPasswordCredentialsGrant) =>
        val resourceOwnerPasswordCredentialsGrant = proto.ResourceOwnerPasswordCredentialsGrant(
          password = r.password,
          username = r.username
        )

        grant.withResourceOwnerPasswordCredentialsGrant(resourceOwnerPasswordCredentialsGrant)
    }

    service
      .validateAccessGrant(proto.ValidateAccessGrantRequest(Some(grant)))
      .handleAndReport(exceptionCollector) { case NonFatal(_) => validationFallbackResponse }
      .map(_.isValid)
  }
}
