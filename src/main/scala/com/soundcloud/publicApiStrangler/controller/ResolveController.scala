package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.BffInjectionBasedController
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

class ResolveController(val mothershipDispatcher: DispatchToMothershipHandler) extends BffInjectionBasedController {
  get("/resolve")(mothershipDispatcher.dispatch)
  get("/resolve.json")(mothershipDispatcher.dispatch)
}
