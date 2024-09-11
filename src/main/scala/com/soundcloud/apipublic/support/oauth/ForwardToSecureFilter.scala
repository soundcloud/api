package com.soundcloud.apipublic.support.oauth

import com.soundcloud.apipublic.client.secure.SecureClient
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.twitter.finagle.http.Response
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future, Try}
import play.api.libs.json.Json

import java.nio.charset.StandardCharsets
import java.util.Base64

class ForwardToSecureFilter(
    grantExchangeRequestMapper: GrantExchangeRequestParser,
    rolloutClient: Rollout,
    secureClient: SecureClient
) extends SimpleFilter[HandlerRequest, Response] {

  override def apply(request: HandlerRequest, service: Service[HandlerRequest, Response]): Future[Response] = {
    rolloutClient.isActive(RolloutFeature("forward_to_secure")).flatMap {
      case true =>
        grantExchangeRequestMapper.parse(request) match {
          case Right(parsedRequest) =>
            parsedRequest.accessGrant match {
              case AuthorizationCodeGrant(code, _) if isJwtFormat(code) =>
                secureClient.forwardToNewTokenEndpoint(request)
              case _ => service(request) // fallback
            }
          case _ => service(request) // fallback
        }
      case false => service(request) // fallback
    }
  }

  /**
    * all JWT's have a header and are separated by periods.
    * here we attempt to parse the header value to confirm the format of the authorization code is in-fact a jwt
    *
    * @param code string used to exchange for access token
    * @return
    */
  private def isJwtFormat(code: String): Boolean = {
    code.split('.').length > 2 &&
    Try(Json.parse(new String(Base64.getDecoder.decode(code.split('.').head), StandardCharsets.UTF_8))).toOption
      .exists(json => (json \ "enc").asOpt[String].isDefined && (json \ "alg").asOpt[String].isDefined)
  }
}
