package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

class LikesController(userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher) extends BffInjectionBasedController {

  get("/tracks/:id/favoriters")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/tracks/:id/favoriters.json")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/tracks/:id/favoriters/:user_id")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/tracks/:id/favoriters/:user_id.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
}
