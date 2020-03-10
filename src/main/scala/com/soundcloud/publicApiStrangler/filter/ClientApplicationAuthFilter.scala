package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

/**
  * Block access to the API based on a blacklist of client application ids
  */
class ClientApplicationAuthFilter(userAuthentication: UserAuthentication, telemetry: Telemetry, router: HandlerRouter)
    extends SimpleFilter[Request, Response] {
  private val unauthorisedClientApplicationCounter = telemetry.counter(
    "unauthorised_client_application_access_total",
    "Number of unauthorised API accesses by client application id",
    "appid",
    "path"
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    userAuthentication.withUserSession(HandlerRequest(request)) { userSession =>
      val clientAppId = Option(userSession.getAgent).map(_.identifier).getOrElse("unknown")
      val path = router.pathMatching(request).rawPattern

      if (ClientApplicationAuthFilter.blackistedApplicationIds.contains(clientAppId)) {
        unauthorisedClientApplicationCounter.labels(clientAppId, path).inc()
        service(request) // No-Op for now
        //Future.value(JsonResponseBuilder.forbidden())
      } else {
        service(request)
      }
    }
  }
}

object ClientApplicationAuthFilter {
  val blackistedApplicationIds = Set(
    "41763", // SoundCloud.com (development)
    "46941", // SoundCloud.com,
    "66152", // SoundCloud MobileWeb development
    "65097", // m.soundcloud.com
    "90575" // SoundCloud Visual Embed Player
  )
}
