package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{ResponseBuilder, Request => BffRequest}
import com.soundcloud.bff.web.BffController
import com.soundcloud.publicApiStrangler.support._
import com.twitter.util.Future

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

  private[controller] def returnNothing(request: BffRequest, feature: String): Future[ResponseBuilder] = {
    withUserSession(request) { session =>
      gatekeeperClient.isFeatureAccessible(session, feature).flatMap { disabled =>
        if (disabled) Future.value(new ResponseBuilder().nothing.status(200))
        else forwardHandler.handle(request)
      }
    }.rescue {
      case e: Exception => forwardHandler.handle(request)
    }
  }
}
