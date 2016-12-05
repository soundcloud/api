package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

class SuggestedUsersController(userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher) extends BffInjectionBasedController {

  get("/me/suggested/users/:category")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/me/suggested/users/:category.json")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/users/suggested")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/suggested.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
}
