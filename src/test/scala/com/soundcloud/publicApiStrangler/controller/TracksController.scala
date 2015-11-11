package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.trackcoordinator.client.mapper.TrackMapper
import com.soundcloud.trackcoordinator.client.TrackCoordinatorClient
import com.soundcloud.jvmkit.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.{Geo => JvmGeo, UserSessionBuilder}
import com.soundcloud.trackcoordinator.client.representation.{Error, Errors, Failure, NotFound, Result, Success, Track => CoordinatorTrack, TrackUpdate}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Geo, Urn}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.{Request, Response, Status}
import com.soundcloud.bff.finagle.ResponseBuilder
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeUtils}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._

class TracksControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val fallback = mock[DispatchToMothershipHandler]
    val trackCoordinator = mock[TrackCoordinatorClient]
    val trackUrn = Urn("soundcloud:tracks:999")
    val track = TrackMapper(trackCoordinatorTrack)

    lazy val geo = Geo("US")
    lazy val session = new UserSessionBuilder().setUser(Urn("soundcloud:users:2")).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()
    lazy val controller = new TracksController(fakeUserAuthentication(session), trackCoordinator, fallback)

    trackCoordinator.deleteTrack(session, trackUrn) returns Future(Success(()))
    trackCoordinator.updateTrack(===(session), ===(trackUrn), any, any) returns Future(Success(track))
    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }


  "GET /tracks/:id" >> {
    "falls back onto moshi" in new Context {
      val response = get(controller, "/tracks/999")
      response.status ==== Status.Ok
    }
  }

  "POST /tracks/:id" >> {
    "falls back onto moshi" in new Context {
      val response = post(controller, "/tracks/999")
      response.status ==== Status.Ok
    }
  }


  "PUT /tracks/:id" >> {
    "succeeds" in new Context {
      val response = put(controller, "/tracks/999", body = trackCoordinatorUpdate)
      response.status ==== Status.Ok
      TrackMapper(response.jsonBody) ==== TrackMapper(trackCoordinatorTrack)
    }

    "not found" in new Context {
      trackCoordinator.updateTrack(===(session), ===(trackUrn), any, any) returns Future(NotFound)

      val response = put(controller, "/tracks/999", body = trackCoordinatorUpdate)
      response.status ==== Status.NotFound
    }

    "errors out in an expected fashion" in new Context {
      trackCoordinator.updateTrack(===(session), ===(trackUrn), any, any) returns Future(Errors(List(Error(412, "OMG SO WRONG"))))

      val response = put(controller, "/tracks/999", body = trackCoordinatorUpdate)
      response.status ==== Status.PreconditionFailed
      val errors = response.jsonBody.as[List[JsObject]]
      (errors(0) \ "status").as[Int] ==== 412
      (errors(0) \ "message").as[String] ==== "OMG SO WRONG"
    }

    "errors out unexpectedly" in new Context {
      trackCoordinator.updateTrack(===(session), ===(trackUrn), any, any) returns Future(Failure)

      val response = put(controller, "/tracks/999", body = trackCoordinatorUpdate)
      response.status ==== Status.InternalServerError
    }
  }

  "DELETE /tracks/:id" >> {
    "succeeds" in new Context {
      val response = delete(controller, "/tracks/999")
      response.status ==== Status.Accepted
    }

    "not found" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(NotFound)

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.NotFound
    }

    "errors out in an expected fashion" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(Errors(List(Error(412, "OMG SO WRONG"))))

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.PreconditionFailed
      val errors = response.jsonBody.as[List[JsObject]]
      (errors(0) \ "status").as[Int] ==== 412
      (errors(0) \ "message").as[String] ==== "OMG SO WRONG"
    }

    "errors out unexpectedly" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(Failure)

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.InternalServerError
    }
  }
}
