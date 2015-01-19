package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{ResponseBuilder, Request}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamRedirectResponseMapper, TrackStreamJsonResponseMapper}
import com.soundcloud.scalakit.{UserSession, Urn}
import com.twitter.util.Future
import org.specs2.mutable.Before


class TrackStreamControllerSpec extends ControllerSpec {

  "TrackStreamController" should {

    trait Context extends Before {
      val user = Urn("soundcloud:users:1234")

      override def before = {
        controller.loggedInAs(user)
      }

      def forwardWithJsonResponseMapper(path: String) = {
        gatekeeperClientMock.isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support")) returns Future.value(true)
        val expectedResponseBuilder = new ResponseBuilder().ok
        trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToJonResponseMapperMock)) returns Future.value(expectedResponseBuilder)
        get(path)

        response.code ==== 200
        there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamJsonResponseMapper])
        there was one(gatekeeperClientMock).isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support"))
        there was noCallsTo(mothershipDispatcherMock)
      }

      def forwardWithRedirectResponseMapper(path: String) = {
        gatekeeperClientMock.isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support")) returns Future.value(true)
        val expectedResponseBuilder = new ResponseBuilder().ok
        trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToRedirectMapperMock)) returns Future.value(expectedResponseBuilder)
        get(path)

        response.code ==== 200
        there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamRedirectResponseMapper])
        there was one(gatekeeperClientMock).isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support"))
        there was noCallsTo(mothershipDispatcherMock)
      }
    }

    "when pub-api-snip-support is enabled" >> {

      "forward request to /tracks/:trackId/streams to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithJsonResponseMapper("/tracks/5/streams")
      }

      "forward request to /tracks/:trackId/streams.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithJsonResponseMapper("/tracks/5/streams.json")
      }

      "forward request to /i1/tracks/:trackId/streams to handler that knows how to deal with snip content type and return response unchanged." in new Context  {
        forwardWithJsonResponseMapper("/i1/tracks/5/streams")
      }

      "forward request to /i1/tracks/:trackId/streams.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithJsonResponseMapper("/i1/tracks/5/streams")
      }

      "forward request to /tracks/:trackId/stream to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithRedirectResponseMapper("/tracks/5/stream")
      }

      "forward request to /tracks/:trackId/stream.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithRedirectResponseMapper("/tracks/5/stream.json")
      }
    }

    "when pub-api-snip-support is not enabled" >> {
      "forward request to handler that forwards to pub api and return response unchanged" in new Context {
        gatekeeperClientMock.isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support")) returns Future.value(false)
        val expectedResponseBuilder = new ResponseBuilder().ok
        mothershipDispatcherMock.dispatch(any[Request]) returns Future.value(expectedResponseBuilder)
        get("/tracks/5/streams")

        response.code ==== 200
        there was one(mothershipDispatcherMock).dispatch(any[Request])
        there was one(gatekeeperClientMock).isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support"))
        there was noCallsTo(trackStreamSnipHandlerMock)
      }
    }

    "when gatekeeper client throws exception" >> {
      "pub-api-snip-support should be disabled and we should forward to pub api" in new Context {
        gatekeeperClientMock.isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support")) returns Future.exception(new IllegalStateException())
        val expectedResponseBuilder = new ResponseBuilder().ok
        mothershipDispatcherMock.dispatch(any[Request]) returns Future.value(expectedResponseBuilder)
        get("/tracks/5/streams")

        response.code ==== 200
        there was one(mothershipDispatcherMock).dispatch(any[Request])
        there was one(gatekeeperClientMock).isFeatureAccessible(any[UserSession], org.mockito.Matchers.eq("pub-api-snip-support"))
        there was noCallsTo(trackStreamSnipHandlerMock)
      }
    }

  }

}
