package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

/** NOTE: This is a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  * These endpoints are NOT properly strangled. */
class SuggestedUsersController(val userAuthentication: UserAuthentication,
                               val mothershipDispatcher: DispatchToMothershipHandler,
                               val followCountsClient: FollowCountsClient,
                               val lieblingClient: LieblingClient)
  extends BffInjectionBasedController with CountsHelper {

  get("/me/suggested/users/:category")(dispatchToMothershipWithFollowCounts)
  get("/me/suggested/users/:category.json")(dispatchToMothershipWithFollowCounts)

  get("/users/suggested")(dispatchToMothershipWithFollowCounts)
  get("/users/suggested.json")(dispatchToMothershipWithFollowCounts)
}
