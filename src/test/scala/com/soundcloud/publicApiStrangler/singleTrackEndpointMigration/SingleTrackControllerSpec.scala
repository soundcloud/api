package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.CollectorRegistry
import org.mockito.Mockito.{verify, when}

class SingleTrackControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val fallback = mock[DispatchToMothershipHandler]
    val tracksService = mock[TrackRepresentationsService]
    val responseComparison = mock[ResponseComparison]

    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config, collectorRegistry)

    val session = new UserSessionBuilder().build()
    val trackUrn = Urn("soundcloud:tracks:987")

    def controller(session: UserSession) = new SingleTrackController(
      fakeUserAuthentication(session),
      fallback,
      tracksService,
      responseComparison,
      telemetry)
  }

  val validPaths = List("/tracks/987", "/tracks/987/", "/tracks/987.json", "/tracks/987.json/")
  val nonNumericPaths = List("/tracks/__12", "/tracks/__12/", "/tracks/permalinktrack", "/tracks/permalinktrack/",
    "/tracks/permalinktrack.json", "/tracks/permalinktrack.json/")

  validPaths.foreach {
    path =>
      s"removes conditional request headers for path: $path" in new Context {
        when(fallback.dispatchToMothership(like[Request] {
          case r =>
            r.headerMap.get("If-None-Match") must beNone
        })).thenReturn(Future.value(Response()))
        when(tracksService.track(session, trackUrn, None, None)).thenReturn(Future.value(Response()))

        val response = get(controller(session), path, Map.empty, Map("If-None-Match" -> "a8d3ba6d09b68691b77dc75dfcd7a477"))
        response.status ==== Status.Ok
      }
  }

  nonNumericPaths.foreach {
    path =>
      s"returns 404 for non-numeric track identifier for path: $path" in new Context {
        val response = get(controller(session), path)
        response.status ==== Status.NotFound
        response.body ==== """{"errors":[{"error_message":"404 - Not Found"}]}"""
        response.getHeaders.get("Content-Length") must beSome("48")
      }
  }

  nonNumericPaths.foreach {
    path =>
      s"returns 404 wrapped in jsonp for non-numeric track identifier when callback param is provided for path: $path" in new Context {
        val response = get(controller(session), path, Map("callback" -> "js_callback_fn"))
        response.status ==== Status.NotFound
        response.body ==== """/**/js_callback_fn({"errors":[{"error_message":"404 - Not Found"}]});"""
        response.getHeaders.get("Content-Length") must beSome("69")
      }
  }

  validPaths.foreach {
    path =>
      s"Passes secret token to tracks service for path: $path" in new Context {
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(Response()))
        when(tracksService.track(session, trackUrn, Some("s3cret"), None)).thenReturn(Future.value(Response()))

        val response = get(controller(session), path, Map("secret_token" -> "s3cret"))
        response.status ==== Status.Ok
      }
  }

  validPaths.foreach {
    path =>
      s"Passes callback parameters to tracks service for path: $path" in new Context {
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(Response()))
        when(tracksService.track(session, trackUrn, None, Some("js_callback_dn"))).thenReturn(Future.value(Response()))

        val response = get(controller(session), path, Map("callback" -> "js_callback_dn"))
        response.status ==== Status.Ok
      }
  }

  validPaths.foreach {
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
            when(tracksService.track(session, trackUrn, None, None)).thenReturn(Future.value(migrationResponse))

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
