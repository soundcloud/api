package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.BffController
import com.soundcloud.publicApiStrangler.support._
import com.twitter.util.Future

trait GroupController extends BffController
with PublicApiClientComponent {

  //XXX: Implemented this during an outage that we had to disable groups endpoints. Remove this as soon is safe
  //HOTFIX:
  get("/users/:id/groups.json")(returnNothing(_))
  get("/groups/:id.json")(returnNothing(_))

  private def returnNothing(request: BffRequest) = {
    Future(new ResponseBuilder().nothing.status(200))
  }
}
