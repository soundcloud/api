package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.support.{DispatchToMothershipHandler, TrackStreamSnipHandler}
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.util.Future

class TrackStreamsControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val user = Urn("soundcloud:users:1234")
    val session = loggedInSession(user)

    val trackStreamUrlToJsonResponseMapperMock = mock[TrackStreamJsonResponseMapper]
    val trackStreamUrlToRedirectMapperMock = mock[TrackStreamRedirectResponseMapper]
    val mothershipDispatcherMock = mock[DispatchToMothershipHandler]
    val trackStreamSnipHandlerMock = mock[TrackStreamSnipHandler]

    val controller = new TrackStreamsController(
      fakeUserAuthentication(session),
      trackStreamUrlToJsonResponseMapperMock,
      trackStreamUrlToRedirectMapperMock,
      mothershipDispatcherMock,
      trackStreamSnipHandlerMock
    )

    def forwardWithJsonResponseMapper(controller: TrackStreamsController, path: String) = {
      val expectedResponseBuilder = new ResponseBuilder().ok

      trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToJsonResponseMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(controller, path)

      response.code ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamJsonResponseMapper])
      there was noCallsTo(mothershipDispatcherMock)
    }

    def forwardWithRedirectResponseMapper(controller: TrackStreamsController, path: String) = {
      val expectedResponseBuilder = new ResponseBuilder().ok

      trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToRedirectMapperMock))
        .returns(Future.value(expectedResponseBuilder))

      val response = get(controller, path)

      response.code ==== 200
      there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamRedirectResponseMapper])
      there was noCallsTo(mothershipDispatcherMock)
    }
  }

  "forward request to /tracks/:trackId/streams to handler that knows how to deal with snip content type and return response unchanged." in new Context {
    forwardWithJsonResponseMapper(controller, "/tracks/5/streams")
  }

  "forward request to /tracks/:trackId/streams.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
    forwardWithJsonResponseMapper(controller, "/tracks/5/streams.json")
  }

  "forward request to /i1/tracks/:trackId/streams to handler that knows how to deal with snip content type and return response unchanged." in new Context {
    forwardWithJsonResponseMapper(controller, "/i1/tracks/5/streams")
  }

  "forward request to /i1/tracks/:trackId/streams.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
    forwardWithJsonResponseMapper(controller, "/i1/tracks/5/streams.json")
  }

  "forward request to /tracks/:trackId/stream to handler that knows how to deal with snip content type and return response unchanged." in new Context {
    forwardWithRedirectResponseMapper(controller, "/tracks/5/stream")
  }

  "forward request to /tracks/:trackId/stream.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
    forwardWithRedirectResponseMapper(controller, "/tracks/5/stream.json")
  }
}
