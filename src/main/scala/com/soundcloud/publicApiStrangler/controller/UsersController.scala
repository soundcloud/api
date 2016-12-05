package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

class UsersController(userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher) extends BffInjectionBasedController {

  get("/users/:id")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id/")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id.json/")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/users/:id/tracks")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id/tracks/")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id/tracks.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id/tracks.json/")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/users/:id/comments")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id/comments/")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id/comments.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/users/:id/comments.json/")(userRelatedMothershipDispatcher.dispatchToMothership _)

  get("/me")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/me/")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/me.json")(userRelatedMothershipDispatcher.dispatchToMothership _)
  get("/me.json/")(userRelatedMothershipDispatcher.dispatchToMothership _)
}
