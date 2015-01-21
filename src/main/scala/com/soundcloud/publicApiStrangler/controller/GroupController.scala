package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice._
import com.twitter.util.Future
import play.api.libs.json.JsValue

trait GroupController extends BffController
with PublicApiClientComponent
with GateKeeperClientComponent{

  val forwardHandler = new ForwardRequestHandler(publicApiClient)

  //XXX: Implemented this during an outage that we had to disable groups endpoints. Remove this as soon is safe
  //HOTFIX:

  // Cheap endpoints that were being scraped that may or may not have caused instability
  get("/users/:id/groups.json")(returnNothing(_, "disable_cheap_groups_endpoints"))
  get("/groups/:id.json")(returnNothing(_, "disable_cheap_groups_endpoints"))
  // Expensive endpoints. Most likely to cause stability issues
  get("/groups/:id/users.json")(returnNothing(_, "disable_expensive_groups_endpoints"))
  get("/groups/:id/users")(returnNothing(_, "disable_expensive_groups_endpoints"))
  // /groups/:id/users.*

  private def returnNothing(request: BffRequest, feature:String): Future[ResponseBuilder] = {
    withUserSession(request) {
      (session: UserSession) => {
        gatekeeperJsonClient.get(session, Path("/features") / (feature + ".json"), Params.empty, Params.empty).flatMap {
          case JsonResponse(OkStatus, body, _, _) => {
            val groups = (body \ "groups").as[Set[JsValue]].headOption

            val disabled = groups match {
              case Some(group) => group.as[String].equals("all")
              case None => false
            }
            if (disabled)
              Future(new ResponseBuilder().nothing.status(200))
            else
              forwardHandler.handle(request)
          }
          case _ =>
            forwardHandler.handle(request)
        }.rescue {
          case e: Exception => forwardHandler.handle(request)
        }
      }
    }
  }
}
