package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.mockito.Mockito.when
import play.api.libs.json.{JsArray, Json}

class UsersHandlerSpec extends UnitSpecification {

  val userRepresentation = UserRepresentationMapper(Fixtures.okidokiUsers.as[JsArray].value.last)
  val path = "/users/123"
  val nonNumericPaths = List(
    "/users/__12",
    "/users/permalinkuser"
  )

  trait Context extends HandlerSpecificationScope {
    val userRepresentationsService = smartMock[UserRepresentationsService]

    val telemetry = Telemetry.createIsolatedInstance

    val session = new UserSessionBuilder().build()
    val userUrn = Urn("soundcloud", "users", "123")

    val handler = new UsersHandler(
      new FakeUserAuthentication(session),
      userRepresentationsService
    )

    override def routingDefinitions = Routing.forUsersHandler(handler)
  }

  nonNumericPaths.foreach { path =>
    s"returns 400 for non-numeric track identifier for path: $path" in new Context {
      val response = get(path)
      response.status ==== Status.BadRequest
    }
  }

  "it returns 200 when a user is found" in new Context {
    when(userRepresentationsService.user(session, userUrn))
      .thenReturn(Future.value(Some(userRepresentation)))

    val response = get(path)
    response.status.code ==== 200

    response.contentString ==== Json.stringify(Json.toJson(userRepresentation))
  }

  "it returns 404 for None" in new Context {
    when(userRepresentationsService.user(session, userUrn))
      .thenReturn(Future.None)

    val response = get(path)
    response.status.code ==== 404
  }
}
