package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.finagle.http.Status
import com.twitter.util.Future

class RepostsController(userAuthentication: UserAuthentication,
                        repostsClient: RepostsClient,
                        fallback: DispatchToMothershipHandler,
                        writeToReposts: () => Future[Boolean])
  extends BffInjectionBasedController {

  put("/e1/me/track_reposts/:id")(createRepost(_, "tracks"))
  put("/e1/me/track_reposts/:id.json")(createRepost(_, "tracks"))

  delete("/e1/me/track_reposts/:id")(deleteRepost(_, "tracks"))
  delete("/e1/me/track_reposts/:id.json")(deleteRepost(_, "tracks"))

  put("/e1/me/playlist_reposts/:id")(createRepost(_, "playlists"))
  put("/e1/me/playlist_reposts/:id.json")(createRepost(_, "playlists"))

  delete("/e1/me/playlist_reposts/:id")(deleteRepost(_, "playlists"))
  delete("/e1/me/playlist_reposts/:id.json")(deleteRepost(_, "playlists"))

  private def createRepost(request: Request, targetType: String): Future[ResponseBuilder] = {
    writeToReposts().flatMap {
      case true =>
        userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
          val target = new Urn(s"soundcloud:$targetType:" + request.routeParams("id"))
          repostsClient.createRepost(session, target, baseUrl(request)).map(renderResult)
        }

      case false =>
        fallback.dispatch(request)
    }
  }

  private def deleteRepost(request: Request, targetType: String): Future[ResponseBuilder] = {
    writeToReposts().flatMap {
      case true =>
        userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
          val target = new Urn(s"soundcloud:$targetType:" + request.routeParams("id"))
          repostsClient.deleteRepost(session, target, baseUrl(request)).map(renderResult)
        }

      case false =>
        fallback.dispatch(request)
    }
  }

  private def baseUrl(request: Request): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }

  private def renderResult(result: Result): ResponseBuilder = result match {
    case Created => render.created
    case Deleted => render.ok
    case AlreadyExists => render.ok
    case NotFound => render.notFound
    case spamBlocked: SpamBlocked => render.status(Status.TooManyRequests.code).json(spamBlocked)
    case Failed => render.internalServerError
  }
}
