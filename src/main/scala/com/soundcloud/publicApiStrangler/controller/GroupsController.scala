package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.util.Future
import play.api.libs.json.JsArray

class GroupsController(val userAuthentication: UserAuthentication,
                       val mothershipDispatcher: DispatchToMothershipHandler,
                       val followCountsClient: FollowCountsClient)
  extends BffInjectionBasedController with FollowCountsHelper {

  get("/groups")(emptyList)
  get("/groups.json")(emptyList)
  get("/groups/")(emptyList)
  get("/groups.json/")(emptyList)

  get("/me/groups")(emptyList)
  get("/me/groups.json")(emptyList)
  get("/me/groups/")(emptyList)
  get("/me/groups.json/")(emptyList)

  get("/groups/:group_id")(notFound)
  get("/groups/:group_id.json")(notFound)

  get("/groups/:group_id/users")(notFound)
  get("/groups/:group_id/users.json")(notFound)

  get("/groups/:group_id/moderators")(notFound)
  get("/groups/:group_id/moderators.json")(notFound)

  get("/groups/:group_id/contributors")(notFound)
  get("/groups/:group_id/contributors.json")(notFound)

  get("/groups/:group_id/members")(notFound)
  get("/groups/:group_id/members.json")(notFound)

  get("/groups/:group_id/tracks")(notFound)
  get("/groups/:group_id/tracks.json")(notFound)

  private def emptyList(request: Request): Future[ResponseBuilder] =
    new ResponseBuilder().ok.json(JsArray()).toFuture

  private def notFound(request: Request): Future[ResponseBuilder] =
    new ResponseBuilder().notFound.toFuture
}
