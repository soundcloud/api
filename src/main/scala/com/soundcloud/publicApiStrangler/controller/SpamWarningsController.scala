package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.sketchy.{AckOk, WarningNotFound, UnknownError}
import com.soundcloud.publicApiStrangler.client.sketchy.SketchyClient

class SpamWarningsController(userAuthenticator: UserAuthentication, sketchyClient: SketchyClient) extends BffInjectionBasedController {

  put("/me/spam_warnings/:warning_id/ack") { request =>
    userAuthenticator.withLoggedInUser(request) { (session, _) =>
      sketchyClient.ack(session, request.routeParams("warning_id").toInt).map {
        case AckOk => render.ok
        case WarningNotFound => render.notFound
        case _: UnknownError => render.internalServerError
      }
    }
  }
}
