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
    val controller = new SingleTrackController(
      fakeUserAuthentication(session),
      fallback,
      tracksService,
      responseComparison,
      telemetry)


    val trackUrn = Urn("soundcloud:tracks:987")
  }

  List("/tracks/__12", "/tracks/__12/").foreach {
    path =>
      s"returns 4xx when fallback returns 4xx on non numeric track ids, path: $path" in new Context {
        val legacyResponse = Response(Status.NotFound)
        when(fallback.dispatchToMothership(any[Request])).thenReturn(Future.value(legacyResponse))

        val response = get(controller, path)

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

        val response = get(controller, path)
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

        val response = get(controller, path)
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

        val response = get(controller, path)
        verify(responseComparison).report(
          any[Response],
          like[Response] { case r => r.status ==== Status.NotFound }
        )
        response.status ==== Status.Ok
      }
  }
}
