package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.service.media._
import com.soundcloud.publicApiStrangler.test.{FakePublicApiSiloing, HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Method, Response, Status}
import com.twitter.util.Future
import org.specs2.specification.core.Fragments
import play.api.libs.json.Json

class TrackStreamsHandlerSpec extends UnitSpecification {

  class FakeTrackAccessRecorderService extends TrackAccessRecorderService(null) {
    override def recordStreamAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn, loggingEnabled: Boolean)(action: => Future[Response]): Future[Response] =
      action
  }

  trait MediaServiceContext extends HandlerSpecificationScope {
    val user = Urn("soundcloud", "users", "1234")
    val session = loggedInSession(user)

    val streamService = mock[StreamService]

    val handler = new TrackStreamsHandler(
      new FakeUserAuthentication(session),
      mock[TrackStreamJsonResponseMapper],
      mock[TrackStreamRedirectResponseMapper],
      streamService,
      new FakeTrackAccessRecorderService,
      new FakePublicApiSiloing
    )

    val trackUrn = Urn("soundcloud", "tracks", "5")

    override def routingDefinitions = Routing.forTrackStreamsHandler(handler)

    val httpMp3 = "http://mp3-progressive"
    val hlsMp3 = "http://mp3-hls"
    val hlsOpus = "http://opus-hls"
    val httpPreviewMp3 = "http://mp3-progressive-preview"

    def call(method: Method, handler: Handler, path: String) = method match {
      case Method.Head => head(handler, path)
      case Method.Get => get(handler, path)
    }
  }

  "with single stream request" >> {
    Fragments.foreach(Seq(
      (Method.Head, "/tracks/5/stream"),
      (Method.Get, "/tracks/5/stream"),
      (Method.Head, "/v1/tracks/5/stream"),
      (Method.Get, "/v1/tracks/5/stream"),
      (Method.Head, "/tracks/5/stream/"),
      (Method.Get, "/tracks/5/stream/"),
      (Method.Head, "/v1/tracks/5/stream/"),
      (Method.Get, "/v1/tracks/5/stream/"),
      (Method.Head, "/tracks/5/stream.json"),
      (Method.Get, "/tracks/5/stream.json"),
      (Method.Head, "/v1/tracks/5/stream.json"),
      (Method.Get, "/v1/tracks/5/stream.json"),
    )) { case (method, path) =>
      s"${method.toString} $path" in new MediaServiceContext {
        streamService.fetchSingle(session, trackUrn, None) returns Future.value(StreamUrl(httpMp3))

        val response = call(method, handler.redirectStreamRequest, path)

        response.statusCode ==== 302
        if (method == Method.Get) {
          Json.parse(response.getContentString) ==== Json.obj(
            "status" -> "302 - Found",
            "location" -> httpMp3
          )
        }
      }
    }
  }

  "with multiple stream requests" >> {
    Fragments.foreach(Seq(
      (Method.Head, "/tracks/5/streams"),
      (Method.Get, "/tracks/5/streams"),
      (Method.Head, "/v1/tracks/5/streams"),
      (Method.Get, "/v1/tracks/5/streams"),
      (Method.Head, "/i1/tracks/5/streams"),
      (Method.Get, "/i1/tracks/5/streams"),
      (Method.Head, "/tracks/5/streams/"),
      (Method.Get, "/tracks/5/streams/"),
      (Method.Head, "/v1/tracks/5/streams/"),
      (Method.Get, "/v1/tracks/5/streams/"),
      (Method.Head, "/i1/tracks/5/streams/"),
      (Method.Get, "/i1/tracks/5/streams/"),
      (Method.Head, "/tracks/5/streams.json"),
      (Method.Get, "/tracks/5/streams.json"),
      (Method.Head, "/v1/tracks/5/streams.json"),
      (Method.Get, "/v1/tracks/5/streams.json"),
      (Method.Head, "/i1/tracks/5/streams.json"),
      (Method.Get, "/i1/tracks/5/streams.json"),
    )) { case (method, path) =>
      s"${method.toString} $path" in new MediaServiceContext {
        streamService.fetchMultiple(session, trackUrn, None) returns Future.value(
          StreamUrls(httpMp3, hlsMp3, Some(hlsOpus), httpPreviewMp3)
        )

        val response = call(method, handler.handleStreamRequest, path)

        response.statusCode ==== 200
        if (method == Method.Get) {
          Json.parse(response.getContentString) ==== Json.obj(
            "http_mp3_128_url" -> httpMp3,
            "hls_mp3_128_url" -> hlsMp3,
            "hls_opus_64_url" -> hlsOpus,
            "preview_mp3_128_url" -> httpPreviewMp3
          )
        }
      }
    }
  }

  "with a secret token" >> {
    trait WithSecretTokenContext extends MediaServiceContext {
      streamService.fetchSingle(session, trackUrn, Some("itsasecret")) returns Future.value(StreamUrl(httpMp3))
    }

    s"should return 302" in new WithSecretTokenContext {
      val resp = get(handler.redirectStreamRequest, "/tracks/5/stream?secret_token=itsasecret")
      resp.status ==== Status.Found
      resp.headerMap("Location") ==== "http://mp3-progressive"
    }
  }
}
