package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Urn
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.CollectorRegistry
import org.mockito.Mockito.{verify, when}

class SingleTrackControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val fallback = mock[DispatchToMothershipHandler]
    val tracksService = mock[TracksService]
    val responseComparison = mock[ResponseComparison]

    val config = new InMemoryConfig
    val collectorRegistry = new CollectorRegistry
    val telemetry = new Telemetry(config, collectorRegistry)

    val session = new UserSessionBuilder().build()

    def controller(rollout: Urn => Future[Boolean]) = new SingleTrackController(
      fakeUserAuthentication(session),
      fallback,
      tracksService,
      responseComparison,
      telemetry,
      rollout)


    val trackUrn = Urn("soundcloud:tracks:987")
  }

  List("/tracks/987", "/tracks/987/").foreach {
    path =>
      s"when rollout is off uses fallback, path: $path" in new Context {
        val legacyResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"
        val legacyResponse = Response()
        legacyResponse.setContentString(legacyResponseString)
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))

        val rollout = (urn: Urn) => {
          urn ==== trackUrn
          Future.False
        }

        val response = get(controller(rollout), path)
        response.status ==== Status.Ok
        response.body ==== legacyResponseString
      }
  }

  List("/tracks/__12", "/tracks/__12/").foreach {
    path =>
      s"returns 4xx when fallback returns 4xx on non numeric track ids, path: $path" in new Context {
        val legacyResponse = Response(Status.NotFound)
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))

        val rollout = (urn: Urn) => Future.True
        val response = get(controller(rollout), path)

        val errCount = collectorRegistry.getSampleValue(
          "non_numeric_track_id",
          Array("statusCode", "system"),
          Array("404", "TEST-APP")
        )
        errCount == 1
      }
  }

  List("/tracks/permalinktrack", "/tracks/permalinktrack/").foreach {
    path =>
      s"returns 200 when fallback returns 200 on non numeric track ids, path: $path" in new Context {
        val legacyResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"
        val legacyResponse = Response()
        legacyResponse.setContentString(legacyResponseString)
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))

        val rollout = (urn: Urn) => Future.True
        val response = get(controller(rollout), path)
        response.status ==== Status.Ok

        val errCount = collectorRegistry.getSampleValue(
          "non_numeric_track_id",
          Array("statusCode", "system"),
          Array("200", "TEST-APP")
        )
        errCount == 1
      }
  }

  List("/tracks/987", "/tracks/987/").foreach {
    path =>
      s"uses fallback on valid track id, path: $path" in new Context {
        val legacyResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"
        val legacyResponse = Response()
        legacyResponse.setContentString(legacyResponseString)
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))

        val migrationResponseObject = SingleTrackPublicApiRepresentation("track", 987, 112)
        val migrationResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":112}"
        when(tracksService.track(session, trackUrn)).thenReturn(Future.value(Some(migrationResponseObject)))

        val rollout = (urn: Urn) => Future.True
        val response = get(controller(rollout), path)
        verify(responseComparison).report(
          like[Response] { case r => r.contentString ==== legacyResponseString },
          like[Response] { case r => r.contentString ==== migrationResponseString }
        )
        response.status ==== Status.Ok
      }
  }

  List("/tracks/987", "/tracks/987/").foreach {
    path =>
      s"reports http status differences between legacy and migration, path: $path" in new Context {
        val legacyResponseString = "{\"kind\":\"track\",\"id\":987,\"user_id\":111}"
        val legacyResponse = Response()
        legacyResponse.setContentString(legacyResponseString)
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))

        when(tracksService.track(session, trackUrn)).thenReturn(Future.None)

        val rollout = (urn: Urn) => Future.True
        val response = get(controller(rollout), path)
        verify(responseComparison).report(
          any[Response],
          like[Response] { case r => r.status ==== Status.NotFound }
        )
        response.status ==== Status.Ok
      }
  }

  List("/tracks/987", "/tracks/987/").foreach {
    path =>
      s"removes conditional request headers, path: $path" in new Context {

        when(fallback.dispatchToMothership(like[Request] {
          case r =>
            r.headerMap.get("If-None-Match") must beNone
        })).thenReturn(Future.value(Response()))

        val rollout = (urn: Urn) => Future.False
        val response = get(controller(rollout), path,Map.empty,Map("If-None-Match" -> "a8d3ba6d09b68691b77dc75dfcd7a477"))

        response.status ==== Status.Ok
      }
  }
}
