package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.publicApiStrangler.client.sketchy.{AckOk, UnknownError, WarningNotFound}
import com.soundcloud.publicApiStrangler.client.sketchy.SketchyClient
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class SpamWarningsController(userAuthenticator: UserAuthentication, sketchyClient: SketchyClient) {

  def handle(request: HandlerRequest): Future[Response] = {
    userAuthenticator.withLoggedInUser(request) { (session, _) =>
      sketchyClient.ack(session, request.routeParams("warning_id").toInt).map {
        case AckOk => ResponseBuilder.ok()
        case WarningNotFound => ResponseBuilder.notFound()
        case _: UnknownError => ResponseBuilder.internalServerError()
      }
    }
  }
}
