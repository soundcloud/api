package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.http.server.HandlerRouter
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.{JsArray, Json}

class PlaylistsWithTracksTelemetryFilter(
    telemetry: Telemetry,
    router: HandlerRouter
) extends SimpleFilter[Request, Response] {

  private val trackCountBuckets = Seq(100d, 200d, 250d, 500d, 1000d)
  private val tracksCounter = telemetry.histogram(
    "tracks_in_playlist_collection",
    "A number of tracks per playlist collection by path",
    Seq("path"),
    trackCountBuckets: _*
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request).map(response => {
      val path = router.pathMatching(request).rawPattern
      path match {
        case "/users/:userId/playlists.json" | "/users/:userId/playlists" | "/playlists.json" | "/playlists" |
            "/me/playlists" =>
          Try {
            val json = Json.parse(response.contentString)
            val playlistCollection = (json \ "collection").as[JsArray].value
            playlistCollection.map(item => (item \ "tracks").as[JsArray].value.size).sum
          } match {
            case Return(tracksCount) => recordTracksPerResponse(path, tracksCount)
            case _ =>
          }
        case _ =>
      }
      response
    })
  }

  private def recordTracksPerResponse(path: String, tracksCount: Int): Unit = {
    tracksCounter.labels(path).observe(tracksCount.toDouble)
  }
}
