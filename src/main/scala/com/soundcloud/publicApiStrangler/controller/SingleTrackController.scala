package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler


class SingleTrackController(userAuthentication: UserAuthentication,
                            mothershipDispatcher: DispatchToMothershipHandler)
  extends BffInjectionBasedController {

  get("/tracks/:trackId")(mothershipDispatcher.dispatch)
  get("/tracks/:trackId/")(mothershipDispatcher.dispatch)
}
