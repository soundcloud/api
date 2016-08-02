package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.publicApiStrangler.SingleTrackEndpointRollout
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Urn
import com.twitter.util.{Throw, Return, Try, Future}


class SingleTrackController(userAuthentication: UserAuthentication,
                            mothershipDispatcher: DispatchToMothershipHandler,
                            tracksService: DispatchToMothershipHandler,
                            singleTrackEndpointRollout: SingleTrackEndpointRollout)
  extends BffInjectionBasedController {

  get("/tracks/:trackId")(renderTrack)
  get("/tracks/:trackId/")(renderTrack)

  private def renderTrack(req: Request): Future[ResponseBuilder] = {
    Try(Urn("soundcloud", "tracks", req.routeParams("trackId"))) match {
      case Throw(_) => Future.value(render.notFound.body("Track id is not valid."))
      case Return(urn) => {
        singleTrackEndpointRollout.strangleSingleTrackEndpoint(urn) flatMap {
          case true => tracksService.dispatch(req)
          case false => mothershipDispatcher.dispatch(req)
        }
      }
    }
  }
}
