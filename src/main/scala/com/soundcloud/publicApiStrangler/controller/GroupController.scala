package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.nextbff.mapper.FetchMapper
import com.soundcloud.bff.nextbff.repository.BulkFetchByUrnRepository
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.finagle.jsonservice._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.Future
import play.api.libs.json.{JsValue, JsArray}

import scala.util.parsing.json.JSON

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

  private def returnNothing(request: BffRequest) : Future[ResponseBuilder] = {
    withUserSession(request) {
      (session: UserSession) => {
        gatekeeperClient.get(session, Path("/features") / "disable_groups.json", Params.empty, Params.empty).flatMap {
          case JsonResponse(OkStatus, body, _, _) => {
            var featureGroups = (body \ "groups").as[Set[JsValue]]
            val disabled = featureGroups.headOption match {
              case Some(group) => group.as[String].equals("all")
              case None => false
            }
            if (disabled) {
              Future(render.nothing.status(200))
            }
            else {
              forwardHandler.handle(request)
            }
           }
          case _ =>
            forwardHandler.handle(request)
        }
      }
    }
  }
}
