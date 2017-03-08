package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Geo, Urn, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{ClientError, NotFound, ServerError, Success}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{TrackmetadataClient, Track => TMTrack}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.Track
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.mockito.Mockito.when
import play.api.libs.json.{Json => PlayJson, _}

class TracksControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope {
    val fallback = mock[DispatchToMothershipHandler]
    val trackCoordinator = mock[TrackCoordinatorClient]
    val okidoki = mock[OkidokiClient]
    val trackmetadataClient = mock[TrackmetadataClient]
    val trackUrn = new Urn("soundcloud:tracks:999")
    val userUrn = new Urn("soundcloud:users:102661606")
    val loggedInUserUrn = Urn("soundcloud:users:2")
    val users = okidokiUsers.as[List[JsObject]]
    val user = users.head
    val track = mock[Track]

    lazy val geo = new Geo("US")
    lazy val session = new UserSessionBuilder().setUser(loggedInUserUrn).setAgent(new Urn("soundcloud:applications:v2")).setGeo(geo).build()
    lazy val controller = new TracksController(fakeUserAuthentication(session), trackCoordinator, okidoki, fallback, trackmetadataClient)

    def trackmetadataResponse: Future[Option[TMTrack]] = Future.value(None)

    trackCoordinator.deleteTrack(session, trackUrn) returns Future(Success(()))
    okidoki.fetch(===(session), ===(Set(userUrn))) returns Future(List(user))
    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(trackmetadataResponse)
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

  List(
    "/tracks/999",
    "/tracks/999.json"
  ).foreach { path => {

    trait PutContext extends Context {
      def trackResponse(supplyChainStatus: Option[String]) =
        Future.value(Some(TrackMetadataTrackBuilder(
          supply_chain_status = supplyChainStatus).build))
    }

    s"PUT $path" >> {
      "passes through requests with supply_chain_status = manual_upload" in new PutContext {
        override def trackmetadataResponse = trackResponse(Some("manual_upload"))

        when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(201).body("Thank you for creating")))

        val response = put(controller, path, body = singleTrack)
        response.status ==== Status.Created
        response.body ==== "Thank you for creating"
      }

      "passes through requests with supply_chain_status = null" in new PutContext {
        override def trackmetadataResponse = trackResponse(None)

        when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(201).body("Thank you for creating")))

        val response = put(controller, path, body = singleTrack)
        response.status ==== Status.Created
        response.body ==== "Thank you for creating"
      }

      "refuses updating tracks with supply_chain_status = supply_chain" in new PutContext {
        override def trackmetadataResponse = trackResponse(Some("supply_chain"))

        val response = put(controller, path, body = singleTrack)
        response.status ==== Status.Unauthorized
        response.jsonBody ==== PlayJson.obj("reason" -> "not allowed")
      }

      "refuses updating tracks with supply_chain_status = banana" in new PutContext {
        override def trackmetadataResponse = trackResponse(Some("banana"))

        val response = put(controller, path, body = singleTrack)
        response.status ==== Status.Unauthorized
        response.jsonBody ==== PlayJson.obj("reason" -> "not allowed")
      }

      "returns not found when track is not returned" in new PutContext {
        override def trackmetadataResponse = Future.value(None)

        val response = put(controller, path, body = singleTrack)
        response.status ==== Status.NotFound
        response.body ==== ""
      }

      "errors if trackmetadata client throws up" in new PutContext {
        override def trackmetadataResponse = Future.exception(new RuntimeException("nooo"))

        val response = put(controller, path, body = singleTrack)
        response.status ==== Status.InternalServerError
        response.body ==== ""
      }
    }

  } }


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
