package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{UserSessionBuilder, Geo => JvmGeo}
import com.soundcloud.publicApiStrangler.client.GobblyClient
import com.soundcloud.publicApiStrangler.client.gobbly.{ClientError => GobblyClientError, Error => GobblyError, Result => GobblyResult, ServerError => GobblyServerError, Success => GobblySuccess}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Geo, Urn}
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.trackcoordinator.client.TrackCoordinatorClient
import com.soundcloud.trackcoordinator.client.mapper.TrackMapper
import com.soundcloud.trackcoordinator.client.representation.{Error, Errors, Failure, NotFound, Success, Track => CoordinatorTrack}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import play.api.libs.json.{Json => PlayJson, _}

class ResolveControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val fallback = mock[DispatchToMothershipHandler]
    lazy val controller = new ResolveController(fallback)
    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }

  "GET /resolve" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/resolve")
      response.status ==== Status.Ok
    }
  }

  "GET /resolve.json" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/resolve.json")
      response.status ==== Status.Ok
    }
  }
}
