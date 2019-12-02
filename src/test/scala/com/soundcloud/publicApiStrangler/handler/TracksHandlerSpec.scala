package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.authorization.Track
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{ClientError, NotFound, ServerError, Success}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{TrackmetadataClient, Track => TMTrack}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}

class TracksHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val fallback = mock[DispatchToMothershipHandler]
    val trackCoordinator = mock[TrackCoordinatorClient]
    val okidoki = mock[OkidokiClient]
    val trackmetadataClient = mock[TrackmetadataClient]
    val trackUrn = Urn("soundcloud", "tracks", "999")
    val userUrn = Urn("soundcloud", "users", "102661606")
    val loggedInUserUrn = Urn("soundcloud", "users", "2")
    val users = okidokiUsers.as[List[JsObject]]
    val user = users.head
    val track = mock[Track]

    lazy val geo = new Geo("US")
    lazy val session = new UserSessionBuilder()
      .setUser(loggedInUserUrn)
      .setAgent(Urn("soundcloud", "applications", "v2"))
      .setGeo(geo)
      .build()
    lazy val handler =
      new TracksHandler(new FakeUserAuthentication(session), trackCoordinator, okidoki, fallback, trackmetadataClient)

    def trackmetadataResponse: Future[Option[TMTrack]] = Future.value(None)

    override def routingDefinitions = Routing.forTracksHandler(handler)

    trackCoordinator.deleteTrack(session, trackUrn) returns Future(Success(()))
    okidoki.fetch(===(session), ===(Set(userUrn))) returns Future(List(user))
    when(fallback.dispatch(any[HandlerRequest])).thenReturn(Future.value(ResponseBuilder.ok()))
    when(trackmetadataClient.track(session, trackUrn)).thenReturn(trackmetadataResponse)
  }

  List(
    "/tracks/999",
    "/tracks/999.json"
  ).foreach { path =>
    {
      trait PutContext extends Context {
        def trackResponse(supplyChainStatus: Option[String]) =
          Future.value(Some(TrackMetadataTrackBuilder(supply_chain_status = supplyChainStatus).build))
      }

      s"PUT $path" >> {
        "passes through requests with supply_chain_status = manual_upload" in new PutContext {
          override def trackmetadataResponse = trackResponse(Some("manual_upload"))

          when(fallback.dispatch(any[HandlerRequest]))
            .thenReturn(Future.value(ResponseBuilder.created("Thank you for creating")))

          val response = put(handler.handlePut, path, body = Json.stringify(singleTrack))
          response.status ==== Status.Created
          response.contentString ==== "Thank you for creating"
        }

        "passes through requests with supply_chain_status = null" in new PutContext {
          override def trackmetadataResponse = trackResponse(None)

          when(fallback.dispatch(any[HandlerRequest]))
            .thenReturn(Future.value(ResponseBuilder.created("Thank you for creating")))

          val response = put(handler.handlePut, path, body = Json.stringify(singleTrack))
          response.status ==== Status.Created
          response.contentString ==== "Thank you for creating"
        }

        "refuses updating tracks with supply_chain_status = supply_chain" in new PutContext {
          override def trackmetadataResponse = trackResponse(Some("supply_chain"))

          val response = put(handler.handlePut, path, body = Json.stringify(singleTrack))
          response.status ==== Status.Unauthorized
          Json.parse(response.contentString) ==== Json.obj("reason" -> "not allowed")
        }

        "refuses updating tracks with supply_chain_status = banana" in new PutContext {
          override def trackmetadataResponse = trackResponse(Some("banana"))

          val response = put(handler.handlePut, path, body = Json.stringify(singleTrack))
          response.status ==== Status.Unauthorized
          Json.parse(response.contentString) ==== Json.obj("reason" -> "not allowed")
        }

        "returns not found when track is not returned" in new PutContext {
          override def trackmetadataResponse = Future.value(None)

          val response = put(handler.handlePut, path, body = Json.stringify(singleTrack))
          response.status ==== Status.NotFound
          response.contentString ==== ""
        }

        "errors if trackmetadata client throws up" in new PutContext {
          override def trackmetadataResponse = Future.exception(new RuntimeException("nooo"))

          val response = put(handler.handlePut, path, body = Json.stringify(singleTrack))
          response.status ==== Status.InternalServerError
          response.contentString ==== ""
        }
      }
    }
  }

  "DELETE /tracks/:id" >> {
    "succeeds" in new Context {
      val response = delete(handler.handleDelete, "/tracks/999")
      response.status ==== Status.Ok
    }

    "not found" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(NotFound)

      val response = delete(handler.handleDelete, "/tracks/999")
      response.status ==== Status.NotFound
    }

    "handles server errors from Track Coordinator" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(ServerError.empty)

      val response = delete(handler.handleDelete, "/tracks/999")
      response.status ==== Status.InternalServerError
    }

    "handles client errors from Track Coordinator" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(ClientError.empty)

      val response = delete(handler.handleDelete, "/tracks/999")
      response.status ==== Status.InternalServerError
    }
  }
}
