package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
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
