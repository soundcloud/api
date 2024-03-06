package com.soundcloud.apipublic.filter

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.filter.ClientApplicationAuthFilter.{
  invalidAuthenticationError,
  invalidResponseTypeError,
  invalidScopeError
}
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

/**
  * Block access to the API based on:
  * * a deny list of client application ids
  * * lack of Authorization header
  * * for /connect - invalid response_type/non-expiring scope
  */
class ClientApplicationAuthFilter(
    userAuthentication: UserAuthentication,
    telemetry: Telemetry,
    router: HandlerRouter
) extends SimpleFilter[Request, Response] {

  private val authParams = Set("auth_token", "oauth_token", "client_id", "consumer_key")

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
    // Muzooka is a webhook service to update users avatar from a third-party service
    if (path == Routing.grantExchangePath || path == Routing.muzookaWebhook) {
      service(request)

      // Check that call to /connect has only allowed response_type=code + non-expiring scope isn't present
    } else if (path == Routing.connectPath) {
      (isResponseTypeAllowed(request), isScopeNonExpiring(request)) match {
        case (false, _) => Future.value(ErrorResponse.forbidden(invalidResponseTypeError))
        case (_, true) => Future.value(ErrorResponse.forbidden(invalidScopeError))
        case _ => service(request)
      }

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
          if (authParams.exists(paramAsType(_) == authType)) deprecatedAuthByAppCounter.labels(clientAppId).inc()

          // exclude allowlisted partners from Auth header enforcement
          if (ClientApplicationAuthFilter.allowlistedApplicationIds.contains(clientAppId)) {
            service(request)
          } else if (isAuthHeaderPresent(authType)) {
            service(request)
          } else {
            Future.value(ErrorResponse(Status.Unauthorized, invalidAuthenticationError))
          }
        }
      }
    }
  }

  private def paramAsType(p: String) = s"${p}_param"

  private def getAuthType(request: Request): String =
    request.params.keys
      .find(authParams.contains)
      .map(paramAsType)
      .orElse(request.headerMap.keys.find("Authorization".equalsIgnoreCase).map(_ => "oauth_header"))
      .getOrElse("unknown")

  private def isResponseTypeAllowed(request: Request): Boolean =
    request.params.get("response_type").exists(value => value.equals("code"))

  private def isAuthHeaderPresent(authType: String): Boolean = authType.equals("oauth_header")

  private def isScopeNonExpiring(request: Request): Boolean = {
    request.params.get("scope").exists(value => value.equals("non-expiring"))
  }
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

  /** Application that are temporary excluded from auth changes due to upgrade complications on their side */
  val allowlistedApplicationIds = Set(
    "313807", // Serato DJ Pro
    "313871", // Serato Lite
    "192783", // Sonos
    "192796", // Sonos Dev - SC GO
    "192798", // Sonos Int
    "192799", // Sonos Test
    "192801", // Sonos Perf
    "192802", // Sonos Stage
    "201750", // Sonos CI
    "277251", // SoundCloud Developer Candidate Playground
    "179373", // Android Pairing Interview
    "313960", // WeDJ
    "313943", // Rekordbox DJ
    "129952", // Traktor DJ
    "313832", // Pioneer DJ DJM-REC
    "283387" // Auxy
  )

  val invalidResponseTypeError = "Authorization is only allowed for response_type=code."
  val invalidScopeError = "Requesting non-expiring tokens is not allowed. Set scope=''."
  val invalidAuthenticationError =
    "A request must contain the Authorization header. For details please refer to https://developers.soundcloud.com/blog/security-updates-api."
}
