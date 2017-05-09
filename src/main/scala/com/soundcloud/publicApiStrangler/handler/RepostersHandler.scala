package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.client.reposts.{Reposts, RepostsClient}
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser.writes
import com.soundcloud.publicApiStrangler.mapping.reposts.{RepostsResponse, RepostsUser}
import com.twitter.finagle.http.{ParamMap, Response}
import com.twitter.util.Future
import play.api.libs.json.Json

class RepostersHandler(userAuthentication: UserAuthentication,
                       repostsClient: RepostsClient,
                       okidokiClient: RichOkidokiClient,
                       followCountsClient: FollowCountsClient,
                       lieblingClient: LieblingClient,
                       shouldLoadCountsFromLiebling: () => Future[Boolean]) {

  def trackReposters = reposters("tracks") _

  def playlistReposters = reposters("playlists") _

  private def reposters(repostableType: String)(request: HandlerRequest): Future[Response] =
    userAuthentication.withUserSession(request) { session =>
      val limit = request.params.get("limit").map(_.toInt).getOrElse(200)
      val linkedPartitioningEnabled = request.params.get("linked_partitioning").contains("1")
      val cursor = request.params.get("cursor")
      val repostable = new Urn(s"""soundcloud:$repostableType:${request.routeParams("id")}""")
      if (limit <= 200)
        repostsClient.reposters(session, repostable, limit, cursor)
          .flatMap(hydrateUsers(session, request, limit, _))
          .map(respond(linkedPartitioningEnabled))
      else Future.value(ResponseBuilder.badRequest())
    }

  private def respond(linkedPartitioningEnabled: Boolean)(result: RepostsResponse[RepostsUser]): Response =
    if (linkedPartitioningEnabled) ResponseBuilder.ok(Json.stringify(Json.toJson(result)))
    else ResponseBuilder.ok(Json.stringify(Json.toJson(result.collection)))

  private def baseUrl(request: HandlerRequest): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }

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


  private def hydrateUsers(session: UserSession, request: HandlerRequest, limit: Int, reposts: Reposts): Future[RepostsResponse[RepostsUser]] = {
    val url = baseUrl(request)
    val repostCounts = repostsClient.getRepostCountsByUrnWithFallback(session, reposts.urns.toSet)
    val followCounts = followCountsClient.counts(session, reposts.urns)
      .map {
        _.map { case value@FollowCounts(user, _, _) =>
          (user, value)
        }.toMap
      }
    val likeCounts =
      shouldLoadCountsFromLiebling().flatMap {
        case true =>
          lieblingClient.userTotalLikeCount(session, reposts.urns)
            .map {
              _.map { case value@UserTotalLikes(user, _, _) =>
                (user, value)
              }.toMap
            }
        case false => Future.value(Map.empty[Urn, UserTotalLikes])
      }

    val hydratedUsers =
      okidokiClient.fetchRepostsUsersWithoutCounts(session,
        reposts.urns.toSet,
        url, 50)

    for {
      (countReposts,
      countFollows,
      countLikes,
      fullUsers) <- Future.join(repostCounts,
        followCounts,
        likeCounts,
        hydratedUsers)
    } yield {
      val users = fullUsers.map { user =>
        val followsCount = countFollows.get(user.urn)
        val repostsCount = countReposts.get(user.urn)
        val likesCount = countLikes.get(user.urn)

        user.copy(maybeFollowCounts = followsCount,
          maybeRepostsCount = repostsCount,
          maybeLikesCount = likesCount)(user.context)
      }
      RepostsResponse(users, nextHref(request, limit, reposts.nextCursor))
    }
  }
}
