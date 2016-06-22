package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

/** NOTE: This is a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  * These endpoints are NOT properly strangled. */
class UserController(val userAuthentication: UserAuthentication,
                     val mothershipDispatcher: DispatchToMothershipHandler,
                     val followCountsClient: FollowCountsClient)
  extends BffInjectionBasedController with FollowCountsHelper {

  get("/users/:id")(dispatchToMothershipWithFollowCounts)
  get("/users/:id.json")(dispatchToMothershipWithFollowCounts)
  get("/users/:id.json/")(dispatchToMothershipWithFollowCounts)

  get("/me")(dispatchToMothershipWithFollowCounts)
  get("/me.json")(dispatchToMothershipWithFollowCounts)
  get("/me.json/")(dispatchToMothershipWithFollowCounts)
}
