package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.BffInjectionBasedController

class RepostersController(userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher) extends BffInjectionBasedController {

  get("/e1/tracks/:id/reposters")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/e1/tracks/:id/reposters.json")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/e1/playlists/:id/reposters")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/e1/playlists/:id/reposters.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
}
