package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class UsersHandler(
    userAuthentication: UserAuthentication,
    userRepresentationsService: UserRepresentationsService
) {
  def user(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      Try(getUserUrn(req.routeParams("id"))) match {
        case Return(urn) =>
          userRepresentationsService.user(session, urn).map {
            case Some(user) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(user)))
            case None => ErrorResponse.notFound("404 - Not Found")
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }
}
