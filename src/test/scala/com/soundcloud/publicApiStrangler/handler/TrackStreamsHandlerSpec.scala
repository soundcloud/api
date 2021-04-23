package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.authorization.policies.Reason
import com.soundcloud.publicApiStrangler.client.media.TrackAccessRecorderClient
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{
  TrackStreamJsonResponseMapper,
  TrackStreamRedirectResponseMapper
}
import com.soundcloud.publicApiStrangler.service.UnavailableByPolicy
import com.soundcloud.publicApiStrangler.service.media._
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Method, Status}
import com.twitter.util.Future
import org.specs2.specification.core.Fragments
import play.api.libs.json.Json

class TrackStreamsHandlerSpec extends UnitSpecification {
  trait MediaServiceContext extends HandlerSpecificationScope {
    val user = Urn("soundcloud", "users", "1234")
    val session = loggedInSession(user)

    val streamService = mock[StreamService]
    val trackAccessClient = mock[TrackAccessRecorderClient]
    trackAccessClient.recordAccess(any[UserSession], any[Urn], anyString, anyBoolean, any[Option[String]]) returns Future
      .value(ResponseBuilder.ok())

    val handler = new TrackStreamsHandler(
      new FakeUserAuthentication(session),
      mock[TrackStreamJsonResponseMapper],
      mock[TrackStreamRedirectResponseMapper],
      streamService,
      new TrackAccessRecorderService(trackAccessClient, mock[Telemetry])
    )

    val trackUrn = Urn("soundcloud", "tracks", "5")

    override def routingDefinitions = Routing.forTrackStreamsHandler(handler)

    val httpMp3 = "http://mp3-progressive"
    val hlsMp3 = "http://mp3-hls"
    val hlsOpus = "http://opus-hls"
    val httpPreviewMp3 = "http://mp3-progressive-preview"

    def call(method: Method, handler: Handler, path: String) = method match {
      case Method.Head => head(path)
      case Method.Get => get(path)
    }
  }

  "with single stream request" >> {
    Fragments.foreach(
      Seq(
        (Method.Head, "/tracks/5/stream"),
        (Method.Get, "/tracks/5/stream")
      )
    ) {
      case (method, path) =>
        "GET /tracks/5/stream" in new MediaServiceContext {
          streamService.fetchUrls(session, trackUrn, None, singleStream = true) returns
            Future.value(RedirectStreamResponse(httpMp3).good)

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

    "records access and logs" in new MediaServiceContext {
      streamService.fetchUrls(session, trackUrn, None, singleStream = true) returns
        Future.value(RedirectStreamResponse(httpMp3).good)

      val response = get("/tracks/5/stream")
      there was one(trackAccessClient).recordAccess(
        ===(session),
        ===(trackUrn),
        ===("stream"),
        ===(true),
        any[Option[String]]
      )
    }
  }

  "with multiple stream requests" >> {
    Fragments.foreach(
      Seq(
        (Method.Head, "/tracks/5/streams"),
        (Method.Get, "/tracks/5/streams")
      )
    ) {
      case (method, path) =>
        s"${method.toString} $path" in new MediaServiceContext {
          streamService.fetchUrls(session, trackUrn, None) returns Future.value(
            MediaStreamUrls(httpMp3, hlsMp3, Some(hlsOpus), Some(httpPreviewMp3)).good
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

    "records access and with logging disabled" in new MediaServiceContext {
      streamService.fetchUrls(session, trackUrn, None) returns Future.value(
        MediaStreamUrls(httpMp3, hlsMp3, Some(hlsOpus), Some(httpPreviewMp3)).good
      )

      val response = get("/tracks/5/streams")
      there was one(trackAccessClient).recordAccess(
        ===(session),
        ===(trackUrn),
        ===("stream"),
        ===(false),
        any[Option[String]]
      )
    }
  }

  "with a secret token" >> {
    trait WithSecretTokenContext extends MediaServiceContext {
      streamService.fetchUrls(session, trackUrn, Some("itsasecret"), singleStream = true) returns Future.value(
        RedirectStreamResponse(httpMp3).good
      )
    }

    s"should return 302" in new WithSecretTokenContext {
      val resp = get("/tracks/5/stream?secret_token=itsasecret")
      resp.status ==== Status.Found
      resp.headerMap("Location") ==== "http://mp3-progressive"
    }
  }

  "when streaming is not found" >> {
    trait StreamingNotAllowedContext extends MediaServiceContext {
      streamService.fetchUrls(session, trackUrn, None, singleStream = true) returns Future.value(NotFound().bad)
    }

    s"should return 404" in new StreamingNotAllowedContext {
      val resp = get("/tracks/5/stream")
      resp.status ==== Status.NotFound
    }
  }

  "when streaming is not allowed" >> {
    trait StreamingNotAllowedContext extends MediaServiceContext {
      streamService.fetchUrls(session, trackUrn, None, singleStream = true) returns Future.value(
        CustomError(UnavailableByPolicy(trackUrn, Reason.GEO)).bad
      )
    }

    s"should return 404" in new StreamingNotAllowedContext {
      val resp = get("/tracks/5/stream")
      resp.status ==== Status.Forbidden
      (Json.parse(resp.contentString) \ "message").as[String] ==== "Sorry, this track is not available in your area."
    }
  }
}
