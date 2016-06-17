package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.BffInjectionBasedController
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

class AnnouncementsController(val mothershipDispatcher: DispatchToMothershipHandler) extends BffInjectionBasedController {
  get("/announcements")(mothershipDispatcher.dispatch)
  get("/announcements.json")(mothershipDispatcher.dispatch)
}
