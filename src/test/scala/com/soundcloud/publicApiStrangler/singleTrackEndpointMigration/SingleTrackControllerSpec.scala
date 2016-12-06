package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.jvmkit.{Urn, UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.controller.SingleTrackController
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.CollectorRegistry
import org.mockito.Mockito.when
import org.specs2.mutable.Before

class SingleTrackControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val fallback = mock[DispatchToMothershipHandler]
    val tracksService = mock[TrackRepresentationsService]

    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config, collectorRegistry)

    val session = new UserSessionBuilder().build()
    val trackUrn = new Urn("soundcloud:tracks:987")

    def shouldRespondWithTrackMetadata = true

    def controller(session: UserSession) = new SingleTrackController(
      fakeUserAuthentication(session),
      fallback,
      tracksService,
      telemetry,
      () => Future.value(shouldRespondWithTrackMetadata))
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
      s"When loading tracks from trackmetadata for: $path" >> {
        trait FromTrackMetadata extends Context {
          override def shouldRespondWithTrackMetadata = true

          val defaultJsonResponse = """{"pass-through":"for sure"}"""

          def newResponse(code: Int) = {
            val response = Response()
            response.setContentString(defaultJsonResponse)
            response.setStatusCode(code)
            response
          }

          def trackMetadataResponse: Future[Response]

          when(tracksService.track(session, trackUrn, None, None)).thenReturn(trackMetadataResponse)
        }

        "it passes 200 through with the json" in new FromTrackMetadata {
          override def trackMetadataResponse = Future.value(newResponse(200))

          val response = get(controller(session), path)
          response.status.code ==== 200
          response.body ==== defaultJsonResponse
        }

        "it passes 404 through with the json" in new FromTrackMetadata {
          override def trackMetadataResponse = Future.value(newResponse(404))

          val response = get(controller(session), path)
          response.status.code ==== 404
          response.body ==== defaultJsonResponse
        }

        "it passes 500 through with the json" in new FromTrackMetadata {
          override def trackMetadataResponse = Future.value(newResponse(500))

          val response = get(controller(session), path)
          response.status.code ==== 500
          response.body ==== defaultJsonResponse
        }

        "it returns 500 when there are exceptions" in new FromTrackMetadata {
          override def trackMetadataResponse = Future.exception(new RuntimeException("BAD THINGS"))

          val response = get(controller(session), path)
          response.status.code ==== 500
        }
      }
  }

  validPaths.foreach {
    path =>
      s"When loading tracks from public-api for: $path" >> {
        trait FromPublicApi extends Context {
          override def shouldRespondWithTrackMetadata = false

          val defaultJsonResponse = """{"pass-through":"for sure"}"""

          def newResponse(code: Int) = {
            val response = Response()
            response.setContentString(defaultJsonResponse)
            response.setStatusCode(code)
            response
          }

          def publicApiResponse: Future[Response]

          when(fallback.dispatchToMothership(any[Request])).thenReturn(publicApiResponse)
        }

        "it passes 200 through with the json" in new FromPublicApi {
          override def publicApiResponse = Future.value(newResponse(200))

          val response = get(controller(session), path)
          response.status.code ==== 200
          response.body ==== defaultJsonResponse
        }

        "it passes 404 through with the json" in new FromPublicApi {
          override def publicApiResponse = Future.value(newResponse(404))

          val response = get(controller(session), path)
          response.status.code ==== 404
          response.body ==== defaultJsonResponse
        }

        "it passes 500 through with the json" in new FromPublicApi {
          override def publicApiResponse = Future.value(newResponse(500))

          val response = get(controller(session), path)
          response.status.code ==== 500
          response.body ==== defaultJsonResponse
        }

        "it returns 500 when there are exceptions" in new FromPublicApi {
          override def publicApiResponse = Future.exception(new RuntimeException("BAD THINGS"))

          val response = get(controller(session), path)
          response.status.code ==== 500
        }

      }
  }
}
