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
      val snipSupportFeature = "pub_api_snip_support"

      override def before = {
        controller.loggedInAs(user)
      }

      def forwardWithJsonResponseMapper(path: String) = {
        controller.config.set("PUB_API_SNIP_SUPPORT_ENABLED", "true")
        val expectedResponseBuilder = new ResponseBuilder().ok
        trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToJsonResponseMapperMock)) returns Future.value(expectedResponseBuilder)
        get(path)

        response.code ==== 200
        there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamJsonResponseMapper])
        there was noCallsTo(mothershipDispatcherMock)
      }

      def forwardWithRedirectResponseMapper(path: String) = {
        controller.config.set("PUB_API_SNIP_SUPPORT_ENABLED", "true")
        val expectedResponseBuilder = new ResponseBuilder().ok
        trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], org.mockito.Matchers.eq(trackStreamUrlToRedirectMapperMock)) returns Future.value(expectedResponseBuilder)
        get(path)

        response.code ==== 200
        there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamRedirectResponseMapper])
        there was noCallsTo(mothershipDispatcherMock)
      }
    }

    "when pub_api_snip_support is enabled" >> {

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
        forwardWithJsonResponseMapper("/i1/tracks/5/streams.json")
      }

      "forward request to /tracks/:trackId/stream to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithRedirectResponseMapper("/tracks/5/stream")
      }

      "forward request to /tracks/:trackId/stream.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
        forwardWithRedirectResponseMapper("/tracks/5/stream.json")
      }
    }

    // TODO: This test fails when all tests are run because the InMemoryConfig does not support updating a value once already set
//    "when pub_api_snip_support is not enabled" >> {
//      "forward request to handler that forwards to pub api and return response unchanged" in new Context {
//        controller.config.set("PUB_API_SNIP_SUPPORT_ENABLED", "false")
//        val expectedResponseBuilder = new ResponseBuilder().ok
//        mothershipDispatcherMock.dispatch(any[Request]) returns Future.value(expectedResponseBuilder)
//        get("/tracks/5/streams")
//
//        response.code ==== 200
//        there was one(mothershipDispatcherMock).dispatch(any[Request])
//        there was noCallsTo(trackStreamSnipHandlerMock)
//      }
//    }


  }

}
