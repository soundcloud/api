package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Urn, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.sketchy.{AckOk, UnknownError, WarningNotFound}
import com.soundcloud.publicApiStrangler.client.sketchy.SketchyClient
import com.soundcloud.scalakit.Geo
import com.twitter.util.Future

class SpamWarningsControllerSpec extends InjectionBasedControllerSpecification {

  trait AckContext extends Scope {
    val sketchyService = mock[SketchyClient]
    val user = Urn("soundcloud:users:1")
    val geo = Geo("US")
    val session = new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()

    lazy val controller = new SpamWarningsController(fakeUserAuthentication(session), sketchyService)
    lazy val anonController = new SpamWarningsController(fakeUserAuthentication(anonymousSession), sketchyService)

    val path = s"/me/spam_warnings/1/ack"
    val requestHeaders = Map("Host" -> "api.example.com")
  }

  "PUT /me/spam_warnings/:warning_id/ack" >> {
    "with anonymous session" in new AckContext {
      val response = put(anonController, path, requestHeaders)

      response.code ==== 401
    }

    "when ack is successful" in new AckContext {
      sketchyService.ack(session, 1).returns(Future.value(AckOk))

      put(controller, path, requestHeaders).code ==== 200
    }

    "when warning is not found" in new AckContext {
      sketchyService.ack(session, 1).returns(Future.value(WarningNotFound))

      put(controller, path, requestHeaders).code ==== 404
    }

    "when an unknown error occurs" in new AckContext {
      sketchyService.ack(session, 1).returns(Future.value(UnknownError(500, "foobar")))

      put(controller, path, requestHeaders).code ==== 500
    }
  }
}