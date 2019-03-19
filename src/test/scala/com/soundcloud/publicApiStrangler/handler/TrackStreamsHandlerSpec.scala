package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.service.media.{StreamNotFoundError, StreamService, StreamUrl, StreamUrls}
import com.soundcloud.publicApiStrangler.test.{FakePublicApiSiloing, HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Method, Status}
import com.twitter.util.Future
import org.specs2.specification.core.Fragments
import play.api.libs.json.Json

class TrackStreamsHandlerSpec extends UnitSpecification {

  trait UrlgenContext extends HandlerSpecificationScope {
    val user = Urn("soundcloud", "users", "1234")
    val session = loggedInSession(user)

    val trackStreamUrlToJsonResponseMapperMock = mock[TrackStreamJsonResponseMapper]
    val trackStreamUrlToRedirectMapperMock = mock[TrackStreamRedirectResponseMapper]
    val trackStreamSnipHandlerMock = mock[TrackStreamHandler]
    val streamService = mock[StreamService]

    val handler = new TrackStreamsHandler(
      new FakeUserAuthentication(session),
      trackStreamUrlToJsonResponseMapperMock,
      trackStreamUrlToRedirectMapperMock,
      trackStreamSnipHandlerMock,
      streamService,
      () => Future.False,
      new FakePublicApiSiloing
    )

    override def routingDefinitions = Routing.forTrackStreamsHandler(handler)

    def forwardWithJsonResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = JsonResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], ===(trackStreamUrlToJsonResponseMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(handler.handleStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamJsonResponseMapper])
    }

    def forwardHeadWithJsonResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = JsonResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], ===(trackStreamUrlToJsonResponseMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = head(handler.handleStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamJsonResponseMapper])
    }

    def forwardWithRedirectResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = JsonResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], ===(trackStreamUrlToRedirectMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(handler.redirectStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamRedirectResponseMapper])
    }

    def forwardHeadWithRedirectResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = JsonResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], ===(trackStreamUrlToRedirectMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = head(handler.redirectStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamRedirectResponseMapper])
    }
  }

  Seq("", "/", ".json").foreach { end: String => {
    Seq("", "/v1").foreach { start: String => {
      s"forward request to ${start}/tracks/:trackId/stream${end} to handler that knows how to deal with snip content type and return response unchanged." in new UrlgenContext {
        forwardWithRedirectResponseMapper(handler, s"${start}/tracks/5/stream${end}")
      }

      s"forward HEAD request to ${start}/tracks/:trackId/stream${end} to handler that knows how to deal with snip content type and return response unchanged." in new UrlgenContext {
        forwardHeadWithRedirectResponseMapper(handler, s"${start}/tracks/5/stream${end}")
      }

      s"forward request to ${start}/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new UrlgenContext {
        forwardWithJsonResponseMapper(handler, s"${start}/tracks/5/streams${end}")
      }

      s"forward HEAD request to ${start}/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new UrlgenContext {
        forwardHeadWithJsonResponseMapper(handler, s"${start}/tracks/5/streams${end}")
      }
    }
    }

    s"forward request to /i1/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new UrlgenContext {
      forwardWithJsonResponseMapper(handler, s"/i1/tracks/5/streams${end}")
    }

    s"forward HEAD request to /i1/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new UrlgenContext {
      forwardHeadWithJsonResponseMapper(handler, s"/i1/tracks/5/streams${end}")
    }
  }
  }

  trait MediaServiceContext extends HandlerSpecificationScope {
    val user = Urn("soundcloud", "users", "1234")
    val session = loggedInSession(user)

    val streamService = mock[StreamService]
    val trackStreamHandler = mock[TrackStreamHandler]

    val handler = new TrackStreamsHandler(
      new FakeUserAuthentication(session),
      mock[TrackStreamJsonResponseMapper],
      mock[TrackStreamRedirectResponseMapper],
      trackStreamHandler,
      streamService,
      () => Future.True,
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

  "media-service to media-urlgen fallback" in new MediaServiceContext {
    streamService.fetchSingle(session, trackUrn, None) returns Future.value(StreamNotFoundError)

    val urlgenResponse = ResponseBuilder().header("Location", "http://stream").status(Status.Found).build
    trackStreamHandler.handle(any[HandlerRequest], any[UserSession], any[TrackStreamRedirectResponseMapper]) returns Future.value(urlgenResponse)

    val resp = get(handler.redirectStreamRequest, "/tracks/5/stream")
    resp.status ==== Status.Found
    resp.headerMap("Location") ==== "http://stream"
  }

}
