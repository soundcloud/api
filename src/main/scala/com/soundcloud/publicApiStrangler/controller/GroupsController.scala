package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.twitter.util.Future
import play.api.libs.json.JsArray

class GroupsController(val userAuthentication: UserAuthentication)
  extends BffInjectionBasedController {

  getWithVariants("/groups")(emptyList)
  getWithVariants("/me/groups")(emptyList)
  getWithVariants("/users/:user_id/groups")(emptyList)
  getWithVariants("/tracks/:track_id/groups")(emptyList)

  getWithVariants("/groups/:group_id")(notFound)
  getWithVariants("/groups/:group_id/users")(notFound)
  getWithVariants("/groups/:group_id/moderators")(notFound)
  getWithVariants("/groups/:group_id/contributors")(notFound)
  getWithVariants("/groups/:group_id/members")(notFound)
  getWithVariants("/groups/:group_id/tracks")(notFound)

  private def variantsOf(s: String) = List(
    s,
    s + '/',
    s + ".json",
    s + ".json/"
  )

  // Same as #get, but also handles variants with .json and with a trailing slash
  private def getWithVariants(s: String)(callback: BffRequestHandler) =
    variantsOf(s).foreach(get(_)(callback))

  private def emptyList(request: Request): Future[ResponseBuilder] =
    new ResponseBuilder().ok.json(JsArray()).toFuture

  private def notFound(request: Request): Future[ResponseBuilder] =
    new ResponseBuilder().notFound.toFuture
}
