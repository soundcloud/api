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

      def forwardWithJsonResponseMapper(path:String) = {
        val expectedResponseBuilder = new ResponseBuilder().ok
        trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], any[TrackStreamJsonResponseMapper]) returns Future.value(expectedResponseBuilder)
        get(path)

        response.code ==== 200
        there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamJsonResponseMapper])
      }

      def forwardWithRedirectResponseMapper(path:String) = {
        val expectedResponseBuilder = new ResponseBuilder().ok
        trackStreamSnipHandlerMock.handle(any[Request], any[UserSession], any[TrackStreamRedirectResponseMapper]) returns Future.value(expectedResponseBuilder)
        get(path)

        response.code ==== 200
        there was one(trackStreamSnipHandlerMock).handle(any[Request], any[UserSession], any[TrackStreamRedirectResponseMapper])
      }
    }

    "forward request to /tracks/:trackId/streams to handler that knows how to deal with snip content type and return response unchanged." in new Context {
      forwardWithJsonResponseMapper("/tracks/5/streams")
    }

    "forward request to /tracks/:trackId/streams.json to handler that knows how to deal with snip content type and return response unchanged." in new Context {
      forwardWithJsonResponseMapper("/tracks/5/streams.json")
    }

    "forward request to /i1/tracks/:trackId/streams to handler that knows how to deal with snip content type and return response unchanged." in new Context {
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

}
