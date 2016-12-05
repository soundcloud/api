package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

/** NOTE: This is a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  * These endpoints are NOT properly strangled. */
class UsersController(val userAuthentication: UserAuthentication,
                      val mothershipDispatcher: DispatchToMothershipHandler,
                      val followCountsClient: FollowCountsClient,
                      val lieblingClient: LieblingClient)
  extends BffInjectionBasedController with CountsHelper {

  get("/users/:id")(dispatchToMothershipWithCounts)
  get("/users/:id/")(dispatchToMothershipWithCounts)
  get("/users/:id.json")(dispatchToMothershipWithCounts)
  get("/users/:id.json/")(dispatchToMothershipWithCounts)

  get("/users/:id/tracks")(dispatchToMothershipWithCounts)
  get("/users/:id/tracks/")(dispatchToMothershipWithCounts)
  get("/users/:id/tracks.json")(dispatchToMothershipWithCounts)
  get("/users/:id/tracks.json/")(dispatchToMothershipWithCounts)

  get("/users/:id/comments")(dispatchToMothershipWithCounts)
  get("/users/:id/comments/")(dispatchToMothershipWithCounts)
  get("/users/:id/comments.json")(dispatchToMothershipWithCounts)
  get("/users/:id/comments.json/")(dispatchToMothershipWithCounts)

  get("/me")(dispatchToMothershipWithCounts)
  get("/me/")(dispatchToMothershipWithCounts)
  get("/me.json")(dispatchToMothershipWithCounts)
  get("/me.json/")(dispatchToMothershipWithCounts)
}
