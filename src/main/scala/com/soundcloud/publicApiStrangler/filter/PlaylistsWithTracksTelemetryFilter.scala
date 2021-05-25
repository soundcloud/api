package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.{JsArray, Json}

class PlaylistsWithTracksTelemetryFilter(
    userAuthentication: UserAuthentication,
    telemetry: Telemetry,
    router: HandlerRouter
) extends SimpleFilter[Request, Response] {

  private val trackCountBuckets = Seq(10d, 50d, 80d, 110d, 150d, 200d)
  private val tracksCounter = telemetry.histogram(
    "tracks_in_playlist_collection",
    "A number of tracks per playlist collection by path and client",
    Seq("path", "appid"),
    trackCountBuckets: _*
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    userAuthentication.withUserSession(HandlerRequest(request)) { session =>
      service(request).map(response => {
        Try {
          val json = Json.parse(response.contentString)
          val playlistCollection = (json \ "collection").as[JsArray].value
          playlistCollection.map(item => (item \ "tracks").as[JsArray].value.size).sum
        } match {
          case Return(tracksCount) =>
            recordTracksPerResponse(request, session, tracksCount)
          case _ =>
        }
        response
      })
    }
  }

  private def recordTracksPerResponse(request: Request, session: UserSession, tracksCount: Int): Unit = {
    val appid = session.getAgent.identifier
    val path = router.pathMatching(request).rawPattern

    tracksCounter.labels(path, appid).observe(tracksCount.toDouble)
  }
}
