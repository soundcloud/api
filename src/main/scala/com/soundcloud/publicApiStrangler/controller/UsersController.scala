package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.service.client.LieblingClient

/** NOTE: This is a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  * These endpoints are NOT properly strangled. */
class UsersController(val userAuthentication: UserAuthentication,
                      val mothershipDispatcher: DispatchToMothershipHandler,
                      val followCountsClient: FollowCountsClient,
                      val lieblingClient: LieblingClient)
  extends BffInjectionBasedController with FollowCountsHelper {

  get("/users/:id")(dispatchToMothershipWithFollowCounts)
  get("/users/:id/")(dispatchToMothershipWithFollowCounts)
  get("/users/:id.json")(dispatchToMothershipWithFollowCounts)
  get("/users/:id.json/")(dispatchToMothershipWithFollowCounts)

  get("/users/:id/tracks")(dispatchToMothershipWithFollowCounts)
  get("/users/:id/tracks/")(dispatchToMothershipWithFollowCounts)
  get("/users/:id/tracks.json")(dispatchToMothershipWithFollowCounts)
  get("/users/:id/tracks.json/")(dispatchToMothershipWithFollowCounts)

  get("/users/:id/comments")(dispatchToMothershipWithFollowCounts)
  get("/users/:id/comments/")(dispatchToMothershipWithFollowCounts)
  get("/users/:id/comments.json")(dispatchToMothershipWithFollowCounts)
  get("/users/:id/comments.json/")(dispatchToMothershipWithFollowCounts)

  get("/me")(dispatchToMothershipWithFollowCounts)
  get("/me/")(dispatchToMothershipWithFollowCounts)
  get("/me.json")(dispatchToMothershipWithFollowCounts)
  get("/me.json/")(dispatchToMothershipWithFollowCounts)
}
