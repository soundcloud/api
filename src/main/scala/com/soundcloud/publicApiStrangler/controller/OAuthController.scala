package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.BffInjectionBasedController
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

class OAuthController(mothershipDispatcher: DispatchToMothershipHandler) extends BffInjectionBasedController {

  post("/oauth2/token")(mothershipDispatcher.dispatch)
  post("/oauth2/token/")(mothershipDispatcher.dispatch)
  post("/oauth2/token.json")(mothershipDispatcher.dispatch)
}
