package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.util.Future

/** NOTE: This is a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  * These endpoints are NOT properly strangled. */
class FriendsController(val userAuthentication: UserAuthentication,
                        val mothershipDispatcher: DispatchToMothershipHandler,
                        val followCountsClient: FollowCountsClient)
  extends BffInjectionBasedController with FollowCountsHelper {

  get("/me/connections/friends")(dispatchToMothershipWithFollowCounts)

  get("/me/connections/friends.json")(dispatchToMothershipWithFollowCounts)
}
