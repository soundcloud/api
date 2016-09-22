package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.support.TrackStreamHandler
import com.soundcloud.publicApiStrangler.test.FakePublicApiSiloing
import com.twitter.util.Future

class TrackStreamsControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val user = new Urn("soundcloud:users:1234")
    val session = loggedInSession(user)

    val trackStreamUrlToJsonResponseMapperMock = mock[TrackStreamJsonResponseMapper]
    val trackStreamUrlToRedirectMapperMock = mock[TrackStreamRedirectResponseMapper]
    val trackStreamSnipHandlerMock = mock[TrackStreamHandler]

    val controller = new TrackStreamsController(
      fakeUserAuthentication(session),
      trackStreamUrlToJsonResponseMapperMock,
      trackStreamUrlToRedirectMapperMock,
      trackStreamSnipHandlerMock,
      new FakePublicApiSiloing
    )

    def forwardWithJsonResponseMapper(controller: TrackStreamsController, path: String) = {
      val expectedResponseBuilder = new ResponseBuilder().ok

      trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToJsonResponseMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(controller, path)

      response.code ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamJsonResponseMapper])
    }

    def forwardHeadWithJsonResponseMapper(controller: TrackStreamsController, path: String) = {
      val expectedResponseBuilder = new ResponseBuilder().ok

      trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToJsonResponseMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = head(controller, path)

      response.code ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamJsonResponseMapper])
    }

    def forwardWithRedirectResponseMapper(controller: TrackStreamsController, path: String) = {
      val expectedResponseBuilder = new ResponseBuilder().ok

      trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToRedirectMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(controller, path)

      response.code ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamRedirectResponseMapper])
    }

    def forwardHeadWithRedirectResponseMapper(controller: TrackStreamsController, path: String) = {
      val expectedResponseBuilder = new ResponseBuilder().ok

      trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToRedirectMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = head(controller, path)

      response.code ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamRedirectResponseMapper])
    }
  }

  Seq("", "/", ".json").foreach { end: String => {
    Seq("", "/v1").foreach { start: String => {
      s"forward request to ${start}/tracks/:trackId/stream${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithRedirectResponseMapper(controller, s"${start}/tracks/5/stream${end}")
      }

      s"forward HEAD request to ${start}/tracks/:trackId/stream${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardHeadWithRedirectResponseMapper(controller, s"${start}/tracks/5/stream${end}")
      }

      s"forward request to ${start}/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithJsonResponseMapper(controller, s"${start}/tracks/5/streams${end}")
      }

      s"forward HEAD request to ${start}/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardHeadWithJsonResponseMapper(controller, s"${start}/tracks/5/streams${end}")
      }
    }
    }

    s"forward request to /i1/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
      forwardWithJsonResponseMapper(controller, s"/i1/tracks/5/streams${end}")
    }

    s"forward HEAD request to /i1/tracks/:trackId/streams${end} to handler that knows how to deal with snip content type and return response unchanged." in new Context {
      forwardHeadWithJsonResponseMapper(controller, s"/i1/tracks/5/streams${end}")
    }
  }
  }
}
