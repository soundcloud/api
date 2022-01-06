package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.apipublic.service.users.MeService
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.outcome._

class MeHandler(
    userAuthentication: UserAuthentication,
    meService: MeService
) {
  def me(req: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(req) { (session, userUrn) =>
      meService.getMe(session, userUrn).map {
        case Good(me) =>
          JsonResponseBuilder(
            Status.Ok,
            body = Json.stringify(Json.toJson(me))
          ).build
        case Bad(_) => JsonResponseBuilder.notFound()
      }
    }
  }
}
