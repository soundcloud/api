package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationFilter
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.CollectorRegistry
import org.mockito.Mockito.{verify, when}

class SingleTrackControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val fallback = mock[DispatchToMothershipHandler]
    val contentAuthorizationFilter = mock[ContentAuthorizationFilter]
    val tracksService = mock[TracksService]
    val responseComparison = mock[ResponseComparison]

    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config, collectorRegistry)

    val session = new UserSessionBuilder().build()
    val trackUrn = Urn("soundcloud:tracks:987")

    def controller(session: UserSession) = new SingleTrackController(
      fakeUserAuthentication(session),
      fallback,
      contentAuthorizationFilter,
      tracksService,
      responseComparison,
      telemetry)
  }

  List("/tracks/987", "/tracks/987/").foreach {
    path =>
      s"removes conditional request headers for path: $path" in new Context {

        when(fallback.dispatchToMothership(like[Request] {
          case r =>
            r.headerMap.get("If-None-Match") must beNone
        })).thenReturn(Future.value(Response()))
        when(contentAuthorizationFilter.apply(any[Request], any[Service[Request, RouterResponse]])).
          thenReturn(Future.value(RouterResponse(Response(), "undefined")))
        when(tracksService.track(session, trackUrn, None)).thenReturn(Future.value(Response()))

        val response = get(controller(session), path, Map.empty, Map("If-None-Match" -> "a8d3ba6d09b68691b77dc75dfcd7a477"))

        response.status ==== Status.Ok
      }
  }

  List("/tracks/987", "/tracks/987/").foreach {
    path =>
      s"Adds magic skip auth content header for path: $path" in new Context {

        when(fallback.dispatchToMothership(like[Request] {
          case r =>
            r.headerMap.get("If-None-Match") must beNone
        })).thenReturn(Future.value(Response()))
        when(contentAuthorizationFilter.apply(any[Request], any[Service[Request, RouterResponse]])).
          thenReturn(Future.value(RouterResponse(Response(), "undefined")))
        when(tracksService.track(session, trackUrn, None)).thenReturn(Future.value(Response()))

        val response = get(controller(session), path, Map.empty, Map("If-None-Match" -> "a8d3ba6d09b68691b77dc75dfcd7a477"))

        response.getHeaders.get(ContentAuthorizationFilter.skipContentAuthHeader) must beSome
      }
  }

  List("/tracks/__12", "/tracks/__12/", "/tracks/permalinktrack", "/tracks/permalinktrack/").foreach {
    path =>
      List(Status.NotFound, Status.Ok).foreach {
        case status =>
          s"uses fallback when there is a non-numeric track identifier for path: $path for status: $status" in new Context {
            val legacyResponse = Response(status)
            when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))
            when(contentAuthorizationFilter.apply(any[Request], any[Service[Request, RouterResponse]])).
              thenReturn(Future.value(RouterResponse(legacyResponse, "undefined")))
            when(tracksService.track(session, trackUrn, None)).thenReturn(Future.value(Response()))

            val response = get(controller(session), path)

            val errCount = collectorRegistry.getSampleValue(
              "non_numeric_track_id",
              Array("statusCode", "system"),
              Array(status.code.toString, "TEST-APP")
            )
            errCount == 1
          }
      }
  }

  List("/tracks/987", "/tracks/987/").foreach {
    path =>
      val legacyResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"
      val legacyResponse = Response()
      legacyResponse.setContentString(legacyResponseString)

      val migrationResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":112}"
      val migrationResponse = Response()
      migrationResponse.setContentString(migrationResponseString)

      List(
        legacyResponse -> migrationResponse,
        legacyResponse -> Response(),
        legacyResponse -> Response(Status.NotFound),
        Response(Status.NotFound) -> migrationResponse
      ).foreach {
        case (legacyResponse, migrationResponse) =>
          val testCaseTitle = s"compares responses where there is a numeric trackidentifier " +
            s"for path: $path for responses ${legacyResponse.status}, ${migrationResponse.status}"
          testCaseTitle in new Context {

            when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))
            when(contentAuthorizationFilter.apply(any[Request], any[Service[Request, RouterResponse]])).
              thenReturn(Future.value(RouterResponse(legacyResponse, "undefined")))
            when(tracksService.track(session, trackUrn, None)).thenReturn(Future.value(migrationResponse))

            val response = get(controller(session), path)

            verify(responseComparison).report(
              any[Request],
              like[Response] { case r =>
                r.contentString ==== legacyResponse.contentString
                r.status ==== legacyResponse.status
              },
              like[Response] {
                case r =>
                  r.contentString ==== migrationResponse.contentString
                  r.status ==== migrationResponse.status
              }
            )
            response.status ==== legacyResponse.status
          }
      }
  }
}
