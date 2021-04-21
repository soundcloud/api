package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.Routing
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class ClientApplicationActivityTelemetryFilter(
    userAuthentication: UserAuthentication,
    telemetry: Telemetry,
    router: HandlerRouter
) extends SimpleFilter[Request, Response] {
  private val counter = telemetry.counter(
    "incoming_http_requests_by_client_total",
    "Number of incoming HTTP requests by application id",
    "method",
    "path",
    "appid"
  )

  override def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern

    if (path == Routing.tokenExchangePath) {
      next(request)
    } else {
      userAuthentication.withUserSession(HandlerRequest(request)) { userSession =>
        val clientAppId = Option(userSession.getAgent).map(_.identifier).getOrElse("unknown")

        if (ClientApplicationActivityTelemetryFilter.targetApplicationIds.contains(clientAppId)) {
          counter
            .labels(
              request.method.name,
              path,
              clientAppId
            )
            .inc()

        }
        next(request)
      }
    }
  }

}

object ClientApplicationActivityTelemetryFilter {
  val targetApplicationIds = Set(
    // APPS ALLOWLISTED IN API-V2
    "59007", // Soundiiz
    "62023", // Soundiiz local
    "167582", // Denon
    "314002", // Denon DJ Embedded / Numark
    "313999", // Denon DJ - Prod
    "314029", // Denon DJ - Test
    "314084", // SCPlanner
    "159540", // repost
    "314057", // Repost Network Official
    "314088", // Repost Network Staging
    "5741", // Tumblr
    "301906", // Miroquai
    "103157", // Soundnode App	
    "300784", // Auryo	
    "304454", // Auryo
    "178345", // Audiu
    "178720", // Audiu Test

    // APPS ALLOWLISTED IN AUTHSY
    "300128", // Google Home production
    "302417", // Google Home development
    "72194", // Temporary hack to enable 3lau (user 1887081) to access their own tracks (see PT-1535) http://www.lessthan3.com
    "164687", // Temporary hack to enable Sara Hartman (user 5095933) to access their own tracks (see https://groups.google.com/a/soundcloud.com/d/msg/partner-tools/jRGskGPH7z4/E1I8YGLzCQAJ)
    "87595", // This American Life
    "67429", // SoundWall Android live wallpaper by Michael England
    "288860", // STAMP Android https://freeyourmusic.com
    "276734", // Babel - www.lucasjgordon.com
    "271862", // Hifi - http://hi.fi
    "265616", // The Playlist Guru - http://spotify-soundcloud.herokuapp.com - http://theplaylist.guru/
    "265183", // The Playlist Guru dev key
    "313993", // Soundmouse ICE
    "91144", // Tune My Music
    "313799" // Linfkire
  )
}
