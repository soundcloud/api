package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.authorization.policies.Reason
import com.soundcloud.apipublic.client.media.TrackAccessRecorderClient
import com.soundcloud.apipublic.service.UnavailableByPolicy
import com.soundcloud.apipublic.service.media._
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
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
      streamService,
      new TrackAccessRecorderService(trackAccessClient)
    )

    val trackUrn = Urn("soundcloud", "tracks", "5")

    override def routingDefinitions = Routing.forTrackStreamsHandler(handler)

    val httpMp3 = "http://mp3-progressive"
    val hlsMp3 = "http://mp3-hls"
    val hlsOpus = "http://opus-hls"
    val aac160k = "http://aac-160k"
    val aac96k = "http://aac-96k"
    val httpPreviewMp3 = "http://mp3-progressive-preview"

    def call(method: Method, path: String) = method match {
      case Method.Head => head(path)
      case Method.Get => get(path)
    }
  }

  "with single stream request" >> {
    Fragments.foreach(
      Seq(
        (Method.Head, "/tracks/soundcloud:tracks:5/stream"),
        (Method.Get, "/tracks/soundcloud:tracks:5/stream")
      )
    ) {
      case (method, path) =>
        "GET /tracks/soundcloud:tracks:5/stream" in new MediaServiceContext {
          streamService.fetchUrls(session, trackUrn, None, singleStream = true) returns
            Future.value(RedirectStreamResponse(httpMp3).good)

          val response = call(method, path)

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

      get("/tracks/soundcloud:tracks:5/stream")
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
        (Method.Head, "/tracks/soundcloud:tracks:5/streams"),
        (Method.Get, "/tracks/soundcloud:tracks:5/streams")
      )
    ) {
      case (method, path) =>
        s"${method.toString} $path" in new MediaServiceContext {
          streamService.fetchUrls(session, trackUrn, None) returns Future.value(
            MediaStreamUrls(
              Some(httpMp3),
              Some(hlsMp3),
              Some(aac96k),
              Some(aac160k),
              Some(hlsOpus),
              Some(httpPreviewMp3)
            ).good
          )

          val response = call(method, path)

          response.statusCode ==== 200
          if (method == Method.Get) {
            Json.parse(response.getContentString) ==== Json.obj(
              "http_mp3_128_url" -> httpMp3,
              "hls_aac_160_url" -> aac160k,
              "hls_aac_96k_url" -> aac96k,
              "hls_mp3_128_url" -> hlsMp3,
              "hls_opus_64_url" -> hlsOpus,
              "preview_mp3_128_url" -> httpPreviewMp3
            )
          }
        }
    }

    "records access and with logging disabled" in new MediaServiceContext {
      streamService.fetchUrls(session, trackUrn, None) returns Future.value(
        MediaStreamUrls(Some(httpMp3), Some(hlsMp3), Some(aac96k), Some(aac160k), Some(hlsOpus), Some(httpPreviewMp3)).good
      )

      get("/tracks/soundcloud:tracks:5/streams")
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
      val resp = get("/tracks/soundcloud:tracks:5/stream?secret_token=itsasecret")
      resp.status ==== Status.Found
      resp.headerMap("Location") ==== "http://mp3-progressive"
    }
  }

  "when streaming is not found" >> {
    trait StreamingNotAllowedContext extends MediaServiceContext {
      streamService.fetchUrls(session, trackUrn, None, singleStream = true) returns Future.value(NotFound().bad)
    }

    s"should return 404" in new StreamingNotAllowedContext {
      val resp = get("/tracks/soundcloud:tracks:5/stream")
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
      val resp = get("/tracks/soundcloud:tracks:5/stream")
      resp.status ==== Status.Forbidden
      (Json.parse(resp.contentString) \ "message").as[String] ==== "Sorry, this track is not available in your area."
    }
  }
}
