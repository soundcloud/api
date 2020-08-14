package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.Status.{Redirection, Successful}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

/**
  * Instrument usage of internal and experimental endpoints
  */
class DeprecatedEndpointUsageFilter(userAuthentication: UserAuthentication, telemetry: Telemetry, router: HandlerRouter)
    extends SimpleFilter[Request, Response] {
  private val deprecatedRequestCounter = telemetry.counter(
    "deprecated_endpoint_requests_total",
    "Number of successful requests to deprecated endpoints by client application",
    "appid",
    "path",
    "method"
  )

  private val deprecatedEndpointsPrefix = Seq(
    "/e1/",
    "/i1/",
    "/tracks/:trackId/related",
    "/me/tracks/:trackId",
    "/me/activities",
    "/me/followings",
    "/me/followers",
    "/me/comments",
    "/me/connections",
    "/me/web-profiles",
    "/users/:id/followings",
    "/users/:id/followers",
    "/users/:userId/web-profiles",
    "/playlists/:playlistId/tracks",
    "/tracks/:id/favoriters/:user_id",
    "/me/email",
    "/me/favorites/ids",
    "/users/:userId/favorites/:trackId",
    "/me/favorites/:trackId",
    "/tracks"
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern

    if (deprecatedEndpointsPrefix.exists(prefix => path.startsWith(prefix))) {
      userAuthentication.withUserSession(HandlerRequest(request)) { session =>
        service(request).map { response =>
          response.status match {
            case Successful(_) => instrumentDeprecatedRequest(session, request, path)
            case Redirection(_) => instrumentDeprecatedRequest(session, request, path)
            case _ =>
          }
          response
        }
      }
    } else {
      service(request)
    }
  }

  private def instrumentDeprecatedRequest(session: UserSession, request: Request, path: String): Unit = {
    val clientAppId = Option(session.getAgent).map(_.identifier).getOrElse("unknown")
    val method = request.method.toString

    deprecatedRequestCounter.labels(clientAppId, path, method).inc()
  }
}
