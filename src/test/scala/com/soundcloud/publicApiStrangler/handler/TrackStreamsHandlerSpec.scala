package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.support.TrackStreamHandler
import com.soundcloud.publicApiStrangler.test.{FakePublicApiSiloing, HandlerSpecificationScope, UnitSpecification}
import com.twitter.util.Future

class TrackStreamsHandlerSpec extends UnitSpecification {

  trait Context extends HandlerSpecificationScope {
    val user = new Urn("soundcloud:users:1234")
    val session = loggedInSession(user)

    val trackStreamUrlToJsonResponseMapperMock = mock[TrackStreamJsonResponseMapper]
    val trackStreamUrlToRedirectMapperMock = mock[TrackStreamRedirectResponseMapper]
    val trackStreamSnipHandlerMock = mock[TrackStreamHandler]

    val handler = new TrackStreamsHandler(
      new FakeUserAuthentication(session),
      trackStreamUrlToJsonResponseMapperMock,
      trackStreamUrlToRedirectMapperMock,
      trackStreamSnipHandlerMock,
      new FakePublicApiSiloing
    )

    override def routingDefinitions = Routing.forTrackStreamsHandler(handler)

    def forwardWithJsonResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = ResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToJsonResponseMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(handler.handleStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamJsonResponseMapper])
    }

    def forwardHeadWithJsonResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = ResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToJsonResponseMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = head(handler.handleStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamJsonResponseMapper])
    }

    def forwardWithRedirectResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = ResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToRedirectMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(handler.redirectStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamRedirectResponseMapper])
    }

    def forwardHeadWithRedirectResponseMapper(handler: TrackStreamsHandler, path: String) = {
      val expectedResponseBuilder = ResponseBuilder.ok()

      trackStreamSnipHandlerMock.handle(any[HandlerRequest], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToRedirectMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = head(handler.redirectStreamRequest, path)

      response.statusCode ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[HandlerRequest], any[UserSession], any[TrackStreamRedirectResponseMapper])
    }
  }

  Seq("", "/", ".json").foreach { end: String => {
    Seq("", "/v1").foreach { start: String => {
      s"forward request to ${start}/tracks/:trackId/stream${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithRedirectResponseMapper(handler, s"${start}/tracks/5/stream${end}")
      }

      s"forward HEAD request to ${start}/tracks/:trackId/stream${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardHeadWithRedirectResponseMapper(handler, s"${start}/tracks/5/stream${end}")
      }

      s"forward request to ${start}/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithJsonResponseMapper(handler, s"${start}/tracks/5/streams${end}")
      }

      s"forward HEAD request to ${start}/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardHeadWithJsonResponseMapper(handler, s"${start}/tracks/5/streams${end}")
      }
    }
    }

    s"forward request to /i1/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
      forwardWithJsonResponseMapper(handler, s"/i1/tracks/5/streams${end}")
    }

    s"forward HEAD request to /i1/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
      forwardHeadWithJsonResponseMapper(handler, s"/i1/tracks/5/streams${end}")
    }
  }
  }
}
