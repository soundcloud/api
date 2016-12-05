package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler

/** NOTE: This is a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  * These endpoints are NOT properly strangled. */
class LikesController(val userAuthentication: UserAuthentication,
                      val mothershipDispatcher: DispatchToMothershipHandler,
                      val followCountsClient: FollowCountsClient,
                      val lieblingClient: LieblingClient)
  extends BffInjectionBasedController with CountsHelper {

  get("/tracks/:id/favoriters")(dispatchToMothershipWithFollowCounts)
  get("/tracks/:id/favoriters.json")(dispatchToMothershipWithFollowCounts)

  get("/tracks/:id/favoriters/:user_id")(dispatchToMothershipWithFollowCounts)
  get("/tracks/:id/favoriters/:user_id.json")(dispatchToMothershipWithFollowCounts)
}
