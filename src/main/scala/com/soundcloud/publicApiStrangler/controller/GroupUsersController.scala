package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.util.Future

/** NOTE: This is a quick-fix in order to fetch follow counts from Stitch instead of Mothership.
  * These endpoints are NOT properly strangled. */
class GroupUsersController(val userAuthentication: UserAuthentication,
                           val mothershipDispatcher: DispatchToMothershipHandler,
                           val followCountsClient: FollowCountsClient)
  extends BffInjectionBasedController with FollowCountsHelper {

  get("/groups/:group_id/users")(dispatchToMothershipWithFollowCounts)
  get("/groups/:group_id/users.json")(dispatchToMothershipWithFollowCounts)

  get("/groups/:group_id/moderators")(dispatchToMothershipWithFollowCounts)
  get("/groups/:group_id/moderators.json")(dispatchToMothershipWithFollowCounts)

  get("/groups/:group_id/contributors")(dispatchToMothershipWithFollowCounts)
  get("/groups/:group_id/contributors.json")(dispatchToMothershipWithFollowCounts)

  get("/groups/:group_id/members")(dispatchToMothershipWithFollowCounts)
  get("/groups/:group_id/members.json")(dispatchToMothershipWithFollowCounts)
}
