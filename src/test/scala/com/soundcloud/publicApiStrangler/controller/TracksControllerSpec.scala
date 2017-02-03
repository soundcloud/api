package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Geo, Urn, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.GobblyClient
import com.soundcloud.publicApiStrangler.client.gobbly.{ClientError => GobblyClientError, Error => GobblyError, Result => GobblyResult, ServerError => GobblyServerError, Success => GobblySuccess}
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{ClientError, NotFound, ServerError, Success}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.Track
import com.soundcloud.jvmkit.ModuleConversions._
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.mockito.Mockito.when
import play.api.libs.json.{Json => PlayJson, _}

class TracksControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope {
    val fallback = mock[DispatchToMothershipHandler]
    val trackCoordinator = mock[TrackCoordinatorClient]
    val okidoki = mock[OkidokiClient]
    val gobblyClient = mock[GobblyClient]
    val trackUrn = new Urn("soundcloud:tracks:999")
    val userUrn = new Urn("soundcloud:users:102661606")
    val users = okidokiUsers.as[List[JsObject]]
    val user = users.head
    val track = mock[Track]

    lazy val geo = new Geo("US")
    lazy val session = new UserSessionBuilder().setUser(new Urn("soundcloud:users:2")).setAgent(new Urn("soundcloud:applications:v2")).setGeo(geo).build()
    lazy val controller = new TracksController(fakeUserAuthentication(session), trackCoordinator, okidoki, fallback, gobblyClient)

    trackCoordinator.deleteTrack(session, trackUrn) returns Future(Success(()))
    okidoki.fetch(===(session), ===(Set(userUrn))) returns Future(List(user))
    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }

  trait ContextWithGobbly extends Context {
    def gobblyResponse: GobblyResult[Boolean] = GobblySuccess(false) // “false” means not HT

    gobblyClient.allTracksManagedByFeedsForWrite(any, ===(List(trackUrn))) returns Future(gobblyResponse)
  }

  "GET /tracks/:id/comments" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/tracks/999/comments")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/tracks/999/comments/")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/tracks/999/comments.json")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json and trailing slash" in new Context {
      val response = get(controller, "/tracks/999/comments.json/")
      response.status ==== Status.Ok
    }
  }

  "GET /tracks/:id/download" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/tracks/999/download")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with trailing slash" in new Context {
      val response = get(controller, "/tracks/999/download/")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json" in new Context {
      val response = get(controller, "/tracks/999/download.json")
      response.status ==== Status.Ok
    }

    "falls back to Mothership with .json and trailing slash" in new Context {
      val response = get(controller, "/tracks/999/download.json/")
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

    "handles server errors from Track Coordinator" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(ServerError.empty)

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.InternalServerError
    }

    "handles client errors from Track Coordinator" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(ClientError.empty)

      val response = delete(controller, "/tracks/999")
      response.status ==== Status.InternalServerError
    }
  }
}
