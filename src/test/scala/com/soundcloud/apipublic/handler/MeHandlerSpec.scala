package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.mothership.response.mapper.{MeMapper, UserRepresentationMapper}
import com.soundcloud.apipublic.service.users.{MeService, UserUploadQuota}
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

class MeHandlerSpec extends UnitSpecification {

  trait Context extends HandlerSpecificationScope {
    val userUrn = Urn("soundcloud", "users", "10419549")
    lazy val geo = new Geo("US")
    lazy val session = {
      new UserSessionBuilder().setUser(userUrn).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()
    }
    val meService = mock[MeService]
    lazy val handler = new MeHandler(
      new FakeUserAuthentication(session),
      meService
    )
    override def routingDefinitions = Routing.forMeHandler(handler)

    val okidokiUser =
      Fixtures.okidokiUsersWithDeprecatedCounts.as[JsArray].value.last

    val userRepresentation = UserRepresentationMapper(okidokiUser)
    val uploadQuota = UserUploadQuota(1, Some(2))
    val expectedMe = MeMapper(okidokiUser, userRepresentation, Some(uploadQuota))
  }

  "GET /me" >> {
    "success" >> {
      "fetches the logged in user" in new Context {
        meService.getMe(session, userUrn) returns Future.value(expectedMe.good)

        val response = get("/me")
        response.status ==== Status.Ok
        Json.parse(response.contentString) ==== Json.toJson(expectedMe)
      }
    }

    "failure" >> {
      "returns 404" in new Context {
        meService.getMe(session, userUrn) returns Future.value(NotFound().bad)

        val response = get("/me")
        response.status ==== Status.NotFound
        response.contentString ==== "{}"
      }
    }
  }
}
