package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.RoutingDefinitions
import com.soundcloud.publicApiStrangler.client.sketchy.{AckOk, SketchyClient, UnknownError, WarningNotFound}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.util.Future

class SpamWarningsControllerSpec extends UnitSpecification {

  trait AckContext extends HandlerSpecificationScope {
    val sketchyService = mock[SketchyClient]
    val user = Urn("soundcloud:users:1")
    val geo = new Geo("US")
    val session = new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()

    lazy val controller = new SpamWarningsController(new FakeUserAuthentication(session), sketchyService)
    lazy val anonController = new SpamWarningsController(new FakeUserAuthentication(anonymousSession), sketchyService)

    override def routingDefinitions = RoutingDefinitions.forSpamWarningsController(controller)

    val path = s"/me/spam_warnings/1/ack"
    val requestHeaders = Map("Host" -> "api.example.com")
  }

  "PUT /me/spam_warnings/:warning_id/ack" >> {
    "with anonymous session" in new AckContext {
      override def routingDefinitions = RoutingDefinitions.forSpamWarningsController(anonController)

      val response = put(anonController.handle, path, requestHeaders)

      response.statusCode ==== 401
    }

    "when ack is successful" in new AckContext {
      sketchyService.ack(session, 1).returns(Future.value(AckOk))

      put(controller.handle, path, requestHeaders).statusCode ==== 200
    }

    "when warning is not found" in new AckContext {
      sketchyService.ack(session, 1).returns(Future.value(WarningNotFound))

      put(controller.handle, path, requestHeaders).statusCode ==== 404
    }

    "when an unknown error occurs" in new AckContext {
      sketchyService.ack(session, 1).returns(Future.value(UnknownError(500, "foobar")))

      put(controller.handle, path, requestHeaders).statusCode ==== 500
    }
  }
}