package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

class FriendsController(userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher) extends BffInjectionBasedController {

  get("/me/connections/friends")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/me/connections/friends.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
}
