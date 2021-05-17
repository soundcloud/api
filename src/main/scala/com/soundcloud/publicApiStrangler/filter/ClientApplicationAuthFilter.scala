package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

/**
  * Block access to the API based on a denylist of client application ids
  */
class ClientApplicationAuthFilter(userAuthentication: UserAuthentication, telemetry: Telemetry, router: HandlerRouter)
    extends SimpleFilter[Request, Response] {
  private val oauthTokenParams = List("auth_token", "oauth_token")
  private val oauthClientParams = List("client_id", "consumer_key")
  private val allOauthParams = (oauthTokenParams ++ oauthClientParams).toSet

  private val unauthorisedClientApplicationCounter = telemetry.counter(
    "unauthorised_client_application_access_total",
    "Number of unauthorised API accesses by client application id",
    "appid",
    "path",
    "method"
  )

  private val appAuthTypeCounter = telemetry.counter(
    "application_auth_type_total",
    "Number of incoming HTTP requests by app auth type",
    "auth_type",
    "path"
  )

  private val deprecatedAuthByAppCounter = telemetry.counter(
    "deprecated_auth_by_app_total",
    "Number of incoming HTTP requests with deprecated auth type by app id",
    "appid"
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern

    // Token exchange accepts client ids in the request body instead of in the params for historical reasons.
    // Authenticator does not parse the body, so we need to exclude the path here to avoid returning 401.
    if (path == Routing.tokenExchangePath) {
      service(request)
    } else {
      userAuthentication.withUserSession(HandlerRequest(request)) { userSession =>
        val clientAppId = Option(userSession.getAgent).map(_.identifier).getOrElse("unknown")
        val authType = getAuthType(request)
        val method = request.method.toString

        if (ClientApplicationAuthFilter.blockedApplicationIds.contains(clientAppId)) {
          unauthorisedClientApplicationCounter.labels(clientAppId, path, method).inc()
          Future.value(ErrorResponse.forbidden())
        } else {
          appAuthTypeCounter.labels(authType, path).inc()
          if (oauthClientParams.exists(paramAsType(_) == authType)) deprecatedAuthByAppCounter.labels(clientAppId).inc()
          service(request)
        }
      }
    }
  }

  private def paramAsType(p: String) = s"${p}_param"

  private def getAuthType(request: Request): String =
    request.params.keys
      .find(allOauthParams.contains)
      .map(paramAsType)
      .orElse(request.headerMap.keys.find("Authorization".equalsIgnoreCase).map(_ => "oauth_header"))
      .getOrElse("unknown")
}

object ClientApplicationAuthFilter {
  val blockedApplicationIds = Set(
    "41763", // SoundCloud.com (development)
    "46941", // SoundCloud.com,
    "66152", // SoundCloud MobileWeb development
    "65097", // m.soundcloud.com
    "90575", // SoundCloud Visual Embed Player
    "43164", // SoundCloud Embed Player,
    "124", // SoundCloud iOS,
    "3152", // SoundCloud Android
    "3537", // SoundCloud Desktop
    "60973" // SoundCloud Flash Widget
  )
}
