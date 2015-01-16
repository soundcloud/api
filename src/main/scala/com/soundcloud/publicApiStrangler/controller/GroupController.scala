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
with PublicApiClientComponent {
  val gatekeeperClient =
    JsonClient(ResourceName("gatekeeper"),
      ServiceEntryPoint(config.get("ROLLOUT_BASE_URL", "http://rollout.int.s-cloud.net")),
      config,
      telemetry)

  val forwardHandler = new ForwardRequestHandler(publicApiClient)

  //XXX: Implemented this during an outage that we had to disable groups endpoints. Remove this as soon is safe
  //HOTFIX:
  get("/users/:id/groups.json")(returnNothing(_))
  get("/groups/:id.json")(returnNothing(_))
  get("/groups/:id/users.json")(returnNothing(_))
  get("/groups/:id/users")(returnNothing(_))

  private def returnNothing(request: BffRequest): Future[ResponseBuilder] = {
    withUserSession(request) {
      (session: UserSession) => {
        gatekeeperClient.get(session, Path("/features") / "disable_groups.json", Params.empty, Params.empty).flatMap {
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
