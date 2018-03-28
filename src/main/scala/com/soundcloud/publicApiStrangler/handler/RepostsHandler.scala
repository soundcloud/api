package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.client.reposts.{Reposts, RepostsClient}
import com.soundcloud.publicApiStrangler.mapper.reposts.representation.RepostsResponse
import com.twitter.finagle.http.{ParamMap, Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

class RepostsHandler(userAuthentication: UserAuthentication, repostsClient: RepostsClient) {

  def createTracksRepost = createRepost(_: HandlerRequest, "tracks")

  def deleteTracksRepost = deleteRepost(_: HandlerRequest, "tracks")

  def getUserRepostableTracks = getUserRepostables(_: HandlerRequest, repostsClient.trackReposts)

  def createPlaylistsRepost = createRepost(_: HandlerRequest, "playlists")

  def deletePlaylistsRepost = deleteRepost(_: HandlerRequest, "playlists")

  def getUserRepostablePlaylists = getUserRepostables(_: HandlerRequest, repostsClient.playlistReposts)

  private def createRepost(request: HandlerRequest, targetType: String): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val target = new Urn(s"soundcloud:$targetType:" + request.routeParams("id"))
      repostsClient.createRepost(session, target, baseUrl(request)).map(renderResult)
    }
  }

  private def deleteRepost(request: HandlerRequest, targetType: String): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val target = new Urn(s"soundcloud:$targetType:" + request.routeParams("id"))
      repostsClient.deleteRepost(session, target, baseUrl(request)).map(renderResult)
    }
  }

  private def getUserRepostables(request: HandlerRequest, callback: (UserSession, Urn, Int, Option[String]) => Future[Reposts]): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      withPaginationParams(request) { (limit, cursor, linkedPartitioningEnabled) =>
        getAllRepostables(session, userUrn, limit, cursor, callback).map { reposts =>
          val ids = reposts.urns.map(_.getIdentifier.toLong)
          respond(linkedPartitioningEnabled)(RepostsResponse(ids, nextHref(request, limit, reposts.nextCursor)))
        }
      }
    }
  }

  private def baseUrl(request: HandlerRequest): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }

  private def renderResult(result: Result): Response = result match {
    case Created => ResponseBuilder.created()
    case Deleted => ResponseBuilder.ok()
    case AlreadyExists => ResponseBuilder.ok()
    case NotFound => ResponseBuilder.notFound()
    case SpamBlocked => JsonResponseBuilder(status = Status.TooManyRequests).build
    case Failed => ResponseBuilder.internalServerError()
  }

  private def getAllRepostables(session: UserSession,
                                user: Urn,
                                limit: Int,
                                cursor: Option[String],
                                callback: (UserSession, Urn, Int, Option[String]) => Future[Reposts]): Future[Reposts] = {
    def nextBatch(acc: Reposts): Future[Reposts] = {
      callback(session, user, RepostsHandler.UpstreamLimit, acc.nextCursor).flatMap {
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

  private def withPaginationParams(request: HandlerRequest)(action: (Int, Option[String], Boolean) => Future[Response]): Future[Response] = {
    val limit = request.params.get("limit").map(_.toInt).getOrElse(200)

    if (limit > 0 && limit <= RepostsHandler.DownstreamMaxLimit) {
      val linkedPartitioningEnabled = request.params.get("linked_partitioning").contains("1")
      val cursor = request.params.get("cursor")
      action(limit, cursor, linkedPartitioningEnabled)
    }
    else {
      Future.value(ResponseBuilder.badRequest())
    }
  }

  private def respond(linkedPartitioningEnabled: Boolean)(result: RepostsResponse[Long]): Response =
    if (linkedPartitioningEnabled) JsonResponseBuilder.ok(Json.stringify(Json.toJson(result)))
    else JsonResponseBuilder.ok(Json.stringify(Json.toJson(result.collection)))

  private def nextHref(request: HandlerRequest, limit: Int, cursor: Option[String]): Option[String] =
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

object RepostsHandler {
  val UpstreamLimit = 200
  val DownstreamMaxLimit = 5000
}
