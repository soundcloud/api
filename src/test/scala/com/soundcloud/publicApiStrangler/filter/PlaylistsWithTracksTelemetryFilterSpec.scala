package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{
  HandlerRequest,
  HandlerRouter,
  HandlerRouterBuilder,
  JsonResponseBuilder
}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistBuilder
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationsSpecificationContext
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class PlaylistsWithTracksTelemetryFilterSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {
    val service = mock[Service[Request, Response]]
    val router = HandlerRouterBuilder()
      .register(Method.Get, "/users/:userId/playlists", _ => Future.value(JsonResponseBuilder.ok()))
      .register(Method.Post, "/foo", _ => Future.value(JsonResponseBuilder.ok()))
      .build

    override val session = new UserSessionBuilder()
      .setUser(Urn("soundcloud", "users", "111"))
      .setAgent(Urn("soundcloud", "application", "999"))
      .build()

    val userAuthentication = new FakeUserAuthentication(session)
    val telemetry = Telemetry.createIsolatedInstance

    val filter =
      new PlaylistsWithTracksTelemetryFilter(
        userAuthentication: UserAuthentication,
        telemetry: Telemetry,
        router: HandlerRouter
      )

    def getHistogramCount(path: String): Option[Double] = {
      telemetry.getSampleValue(
        "tracks_in_playlist_collection_count",
        Seq("path", "appid"),
        Seq(path, "999")
      )
    }

    def getHistogramBucket(path: String, le: String): Option[Double] = {
      telemetry.getSampleValue(
        "tracks_in_playlist_collection_bucket",
        Seq("path", "appid", "le"),
        Seq(path, "999", le)
      )
    }
  }

  trait PlaylistWithTracksResponseContext extends Context {
    val request = HandlerRequest(Request("/users/111/playlists", ("client_id", "999")))
    val path = "/users/:userId/playlists"

    lazy val tracks = List(createTrackRepresentation, createTrackRepresentation, createTrackRepresentation)
    lazy val playlist1 = new PlaylistBuilder().setTracks(Some(tracks)).build
    lazy val playlist2 = new PlaylistBuilder().setTracks(Some(tracks)).build
    lazy val playlistsCollection = Collection(List(playlist1, playlist2), None)

    lazy val json = Collection.getRepresentation(playlistsCollection, true)
    lazy val expectedResponse = jsonCollectionResponse(Status.Ok, json)

    service.apply(any[HandlerRequest]) returns Future.value(expectedResponse)
  }

  "should record tracks count for a response with a playlist collection" in new PlaylistWithTracksResponseContext {
    Await.result(filter.apply(request, service)) ==== expectedResponse

    getHistogramCount(path) === Some(1.0)
    getHistogramBucket(path, "10.0") === Some(1.0)

  }

  "should not record tracks count for a response with a playlist collection w/o tracks" in new PlaylistWithTracksResponseContext {
    lazy val playlist = new PlaylistBuilder().build
    override lazy val playlistsCollection = Collection(List(playlist), None)

    Await.result(filter.apply(request, service)) ==== expectedResponse

    getHistogramCount(path) === None
  }

  trait TrackResponseContext extends Context {
    val request = HandlerRequest(Request("/foo", ("client_id", "999")))

    lazy val track = createTrackRepresentation
    lazy val json = Json.toJson(track)
    lazy val expectedResponse = jsonResponse(Status.Ok, json)

    service.apply(any[HandlerRequest]) returns Future.value(expectedResponse)
  }

  "should not record tracks count for a different response type" in new TrackResponseContext {
    Await.result(filter.apply(request, service)) ==== expectedResponse

    getHistogramCount("/foo") === None
  }

}
