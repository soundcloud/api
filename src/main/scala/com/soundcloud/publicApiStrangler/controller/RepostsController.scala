package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.client.reposts.{Reposts, RepostsClient}
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsResponse
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.Future

class RepostsController(userAuthentication: UserAuthentication,
                        repostsClient: RepostsClient,
                        fallback: DispatchToMothershipHandler)
  extends BffInjectionBasedController {

  put("/e1/me/track_reposts/:id")(createRepost(_, "tracks"))
  put("/e1/me/track_reposts/:id.json")(createRepost(_, "tracks"))

  delete("/e1/me/track_reposts/:id")(deleteRepost(_, "tracks"))
  delete("/e1/me/track_reposts/:id.json")(deleteRepost(_, "tracks"))

  put("/e1/me/playlist_reposts/:id")(createRepost(_, "playlists"))
  put("/e1/me/playlist_reposts/:id.json")(createRepost(_, "playlists"))

  delete("/e1/me/playlist_reposts/:id")(deleteRepost(_, "playlists"))
  delete("/e1/me/playlist_reposts/:id.json")(deleteRepost(_, "playlists"))

  get("/e1/me/track_reposts/ids")(getUserRepostables(_, repostsClient.trackReposts))
  get("/e1/me/track_reposts/ids.json")(getUserRepostables(_, repostsClient.trackReposts))

  get("/e1/me/playlist_reposts/ids")(getUserRepostables(_, repostsClient.playlistReposts))
  get("/e1/me/playlist_reposts/ids.json")(getUserRepostables(_, repostsClient.playlistReposts))

  private def createRepost(request: Request, targetType: String): Future[ResponseBuilder] = {
      userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
        val target = new Urn(s"soundcloud:$targetType:" + request.routeParams("id"))
        repostsClient.createRepost(session, target, baseUrl(request)).map(renderResult)
      }
  }

  private def deleteRepost(request: Request, targetType: String): Future[ResponseBuilder] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val target = new Urn(s"soundcloud:$targetType:" + request.routeParams("id"))
      repostsClient.deleteRepost(session, target, baseUrl(request)).map(renderResult)
    }
  }

  private def getUserRepostables(request: Request, callback: (UserSession, Urn, Int, Option[String]) => Future[Reposts]): Future[ResponseBuilder] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      withPaginationParams(request) { (limit, cursor, linkedPartitioningEnabled) =>
        getAllRepostables(session, userUrn, limit, cursor, callback).map { reposts =>
          val ids = reposts.urns.map(_.getIdentifier.toLong)
          respond(linkedPartitioningEnabled)(RepostsResponse(ids, nextHref(request, limit, reposts.nextCursor)))
        }
      }
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

  private def getAllRepostables(session: UserSession,
                                user: Urn,
                                limit: Int,
                                cursor: Option[String],
                                callback: (UserSession, Urn, Int, Option[String]) => Future[Reposts]): Future[Reposts] = {
    def nextBatch(acc: Reposts): Future[Reposts] = {
      callback(session, user, RepostsController.UpstreamLimit, acc.nextCursor).flatMap {
        case Reposts(urns, None) =>
          Future.value(Reposts(acc.urns ++ urns, None))

        case Reposts(urns, nextCursor) =>
          val allUrns = acc.urns ++ urns
          if (allUrns.length >= limit)
            Future.value(Reposts(allUrns, nextCursor))
          else
            nextBatch(Reposts(allUrns, nextCursor))
      }
    }

    nextBatch(Reposts(List.empty, cursor))
  }

  private def withPaginationParams(request: Request)(action: (Int, Option[String], Boolean) => Future[ResponseBuilder]): Future[ResponseBuilder] = {
    val limit = request.params.get("limit").map(_.toInt).getOrElse(200)

    if (limit > 0 && limit <= RepostsController.DownstreamMaxLimit) {
      val linkedPartitioningEnabled = request.params.get("linked_partitioning").contains("1")
      val cursor = request.params.get("cursor")
      action(limit, cursor, linkedPartitioningEnabled)
    }
    else {
      Future.value(render.badRequest)
    }
  }

  private def respond(linkedPartitioningEnabled: Boolean)(result: RepostsResponse[Long]): ResponseBuilder =
    if (linkedPartitioningEnabled) render.json(result)
    else render.json(result.collection)

  private def nextHref(request: Request, limit: Int, cursor: Option[String]): Option[String] =
    cursor.map { c =>
      val url = baseUrl(request)
      val path = request.path
      val params = request.params ++ ParamMap(
        "linked_partitioning" -> "1",
        "limit" -> limit.toString,
        "cursor" -> c
      )
      s"$url$path${params.toString()}"
    }
}

object RepostsController {
  val UpstreamLimit = 200
  val DownstreamMaxLimit = 5000
}
