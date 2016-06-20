package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Geo => JvmGeo}
import com.soundcloud.publicApiStrangler.client.gobbly.{ClientError => GobblyClientError, Error => GobblyError, Result => GobblyResult, ServerError => GobblyServerError, Success => GobblySuccess}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.trackcoordinator.client.representation.{Track => CoordinatorTrack}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import play.api.libs.json.{Json => PlayJson}

class AnnouncementsControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val fallback = mock[DispatchToMothershipHandler]
    lazy val controller = new AnnouncementsController(fallback)
    when(fallback.dispatch(any[Request])).thenReturn(Future.value(new ResponseBuilder().status(200)))
  }

  "GET /announcements" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/announcements")
      response.status ==== Status.Ok
    }
  }

  "GET /announcements.json" >> {
    "falls back to Mothership" in new Context {
      val response = get(controller, "/announcements.json")
      response.status ==== Status.Ok
    }
  }
}
