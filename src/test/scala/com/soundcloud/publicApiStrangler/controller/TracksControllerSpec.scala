package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.publicApiStrangler.mapper.trackcoordinator.TrackCoordinatorMapper
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.trackcoordinator.client.mapper.TrackMapper
import com.soundcloud.trackcoordinator.client.TrackCoordinatorClient
import com.soundcloud.jvmkit.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.{UserSessionBuilder, Geo => JvmGeo}
import com.soundcloud.publicApiStrangler.client.GobblyClient
import com.soundcloud.publicApiStrangler.client.gobbly.{ClientError => GobblyClientError, Error => GobblyError, Result => GobblyResult, ServerError => GobblyServerError, Success => GobblySuccess}
import com.soundcloud.trackcoordinator.client.representation.{Error, Errors, Failure, NotFound, Result, Success, TrackUpdate, Track => CoordinatorTrack}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Geo, Urn}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.{Request, Response, Status}
import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.publicApiStrangler.client.GobblyClient
import com.soundcloud.scalakit.json.Json
import play.api.libs.json.{Json => PlayJson}
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeUtils}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._

class TracksControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val fallback = mock[DispatchToMothershipHandler]
    val trackCoordinator = mock[TrackCoordinatorClient]
    val okidoki = mock[OkidokiClient]
    val gobblyClient = mock[GobblyClient]
    val trackUrn = Urn("soundcloud:tracks:999")
    val userUrn = Urn("soundcloud:users:102661606")
    val users = okidokiUsers.as[List[JsObject]]
    val user = users.head
    val track = TrackMapper(trackCoordinatorTrack)

    lazy val geo = Geo("US")
    lazy val session = new UserSessionBuilder().setUser(Urn("soundcloud:users:2")).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()
    lazy val controller = new TracksController(fakeUserAuthentication(session), trackCoordinator, okidoki, fallback, gobblyClient)

    trackCoordinator.deleteTrack(session, trackUrn) returns Future(Success(()))
    trackCoordinator.updateTrack(===(session), ===(trackUrn), any, any) returns Future(Success(track))
    trackCoordinator.fetchTrack(===(session), ===(trackUrn), any) returns Future(Success(track))
    okidoki.fetch(===(session), ===(Set(userUrn))) returns Future(List(user))
    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }

  trait ContextWithGobbly extends Context {
    def gobblyResponse: GobblyResult[Boolean] = GobblySuccess(false) // “false” means not HT

    gobblyClient.allTracksManagedByFeedsForWrite(any, ===(List(trackUrn))) returns Future(gobblyResponse)
  }

  "GET /tracks/:id" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/tracks/999")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/tracks/999.json")
      response.status ==== Status.Ok
    }
  }

  "POST /tracks/:id" >> {
    "falls back to Mothership" in new Context {
      val response = post(controller, "/tracks/999")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = post(controller, "/tracks/999.json")
      response.status ==== Status.Ok
    }
  }

  "PUT /tracks/:id" >> {
    "passes through requests" in new ContextWithGobbly {
      when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(201).body("Thank you for creating")))

      val response = put(controller, "/tracks/999", body = singleTrack)
      response.status ==== Status.Created
      response.body ==== "Thank you for creating"
    }

    "refuses updating HT tracks" in new ContextWithGobbly {
      override def gobblyResponse = GobblySuccess(true)

      val response = put(controller, "/tracks/999", body = singleTrack)
      response.status ==== Status.Unauthorized
      response.jsonBody ==== PlayJson.obj("reason" -> "not allowed")
    }

    "errors if Gobbly server throws up" in new ContextWithGobbly {
      override def gobblyResponse = GobblyServerError(GobblyError("blergh"))

      val response = put(controller, "/tracks/999", body = singleTrack)
      response.status ==== Status.InternalServerError
      response.body ==== ""
    }

    "errors if Gobbly client throws up" in new ContextWithGobbly {
      override def gobblyResponse = GobblyClientError(GobblyError("blergh"))

      val response = put(controller, "/tracks/999", body = singleTrack)
      response.status ==== Status.InternalServerError
      response.body ==== ""
    }
  }

  "PUT /tracks/:id.json" >> {
    "passes through requests" in new ContextWithGobbly {
      when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(201).body("Thank you for creating")))

      val response = put(controller, "/tracks/999.json", body = singleTrack)
      response.status ==== Status.Created
      response.body ==== "Thank you for creating"
    }

    "refuses updating HT tracks" in new ContextWithGobbly {
      override def gobblyResponse = GobblySuccess(true)

      val response = put(controller, "/tracks/999.json", body = singleTrack)
      response.status ==== Status.Unauthorized
      response.jsonBody ==== PlayJson.obj("reason" -> "not allowed")
    }

    "errors if Gobbly server throws up" in new ContextWithGobbly {
      override def gobblyResponse = GobblyServerError(GobblyError("blergh"))

      val response = put(controller, "/tracks/999.json", body = singleTrack)
      response.status ==== Status.InternalServerError
      response.body ==== ""
    }

    "errors if Gobbly client throws up" in new ContextWithGobbly {
      override def gobblyResponse = GobblyClientError(GobblyError("blergh"))

      val response = put(controller, "/tracks/999.json", body = singleTrack)
      response.status ==== Status.InternalServerError
      response.body ==== ""
    }
  }

  "DELETE /tracks/:id" >> {
    "succeeds" in new Context {
      val response = delete(controller, "/tracks/999")
      response.status ==== Status.Ok
    }

    "not found" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(NotFound)

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.NotFound
    }

    "errors out in an expected fashion" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(Errors(List(Error(412, "OMG SO WRONG"))))

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.InternalServerError
    }

    "errors out unexpectedly" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(Failure)

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.InternalServerError
    }
  }
}
