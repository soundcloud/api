package com.soundcloud.publicApiStrangler.service.oauth

import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.publicApiStrangler.support.oauth._
import com.twitter.util.Future
import proto.soundcloud.authenticator.{oauth => proto}

import scala.util.control.NonFatal

class AuthorizationService(service: proto.AuthorizationService, exceptionCollector: ExceptionCollector) {
  private val validationFallbackResponse = proto.ValidateAccessGrantResponse(false)

  def validateAccessGrant(
      clientCredential: ClientCredential,
      accessGrant: AccessGrant,
      context: RequestContext
  ): Future[Boolean] = {
    val credential =
      proto.ClientCredential(clientCredential.id, clientCredential.secret)

    val grantType: proto.AccessGrant.GrantType =
      accessGrant match {
        case a: AuthorizationCodeGrant =>
          proto.AccessGrant.GrantType.AuthorizationCodeGrant(proto.AuthorizationCodeGrant(a.code, a.redirectUri))
        case _: ClientCredentialsGrant =>
          proto.AccessGrant.GrantType.ClientCredentialsGrant(proto.ClientCredentialsGrant())
        case r: RefreshTokenGrant =>
          proto.AccessGrant.GrantType.RefreshTokenGrant(proto.RefreshTokenGrant(r.refreshToken))
        case r: ResourceOwnerPasswordCredentialsGrant =>
          proto.AccessGrant.GrantType
            .ResourceOwnerPasswordCredentialsGrant(proto.ResourceOwnerPasswordCredentialsGrant(r.password, r.username))
      }

    val grant =
      proto.AccessGrant(Some(credential), grantType, Some(proto.Context(context.remoteIp, context.userAgent)))

    service
      .validateAccessGrant(proto.ValidateAccessGrantRequest(Some(grant)))
      .handleAndReport(exceptionCollector) { case NonFatal(_) => validationFallbackResponse }
      .map(_.isValid)
  }
}
