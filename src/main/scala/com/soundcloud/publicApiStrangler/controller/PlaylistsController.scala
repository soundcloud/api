package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.service.client.OkidokiClient

/**
  * Overrides the public api endpoints for playlists and switches to
  * moshimoshi playlists controller.
  * Reason for overriding is to make use of a single mothership
  * controller to manage playlists (moshimoshi) to ease carving out a separate
  * playlists service in near future.
  */
class PlaylistsController(userAuthentication: UserAuthentication,
                          okidokiClient: OkidokiClient,
                          mothershipDispatcher: DispatchToMothershipHandler)
  extends BffInjectionBasedController {

  post("/playlists")(mothershipDispatcher.dispatch(_))

  put("/playlists/:id")(mothershipDispatcher.dispatch(_))
  put("/playlists/:id.json")(mothershipDispatcher.dispatch(_))

  delete("/playlists/:id")(mothershipDispatcher.dispatch(_))
  delete("/playlists/:id.json")(mothershipDispatcher.dispatch(_))
}
