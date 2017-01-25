package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapping.reposts.Reposters
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser.writes
import com.soundcloud.publicApiStrangler.client.reposts.{RepostsClient, Reposts}
import com.soundcloud.publicApiStrangler.client.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.twitter.util.Future

class RepostersController(userAuthentication: UserAuthentication,
                          repostsClient: RepostsClient,
                          okidokiClient: RichOkidokiClient,
                          followCountsClient: FollowCountsClient,
                          lieblingClient: LieblingClient,
                          shouldLoadCountsFromReposts: () => Future[Boolean],
                          shouldLoadCountsFromLiebling: () => Future[Boolean])
    extends BffInjectionBasedController {

  get("/e1/tracks/:id/reposters")(reposters("tracks"))
  get("/e1/tracks/:id/reposters.json")(reposters("tracks"))

  get("/e1/playlists/:id/reposters")(reposters("playlists"))
  get("/e1/playlists/:id/reposters.json")(reposters("playlists"))

  private def reposters(repostableType: String)(request: Request): Future[ResponseBuilder] =
    userAuthentication.withUserSession(request) { session =>
      val limit = request.params.get("limit").map(_.toInt).getOrElse(200)
      val linkedPartitioningEnabled = request.params.get("linked_partitioning")
        .filter(_ == "1").isDefined
      val cursor = request.params.get("cursor")
      val repostable = new Urn(s"""soundcloud:$repostableType:${request.routeParams("id")}""")
      if(limit <= 200)
        repostsClient.reposters(session, repostable, limit, cursor)
          .flatMap(hydrateUsers(session, request, limit, _))
          .map(respond(linkedPartitioningEnabled))
      else Future.value(render.badRequest)
    }

  private def respond(linkedPartitioningEnabled: Boolean)(result: Reposters): ResponseBuilder =
    if(linkedPartitioningEnabled) render.json(result)
    else render.json(result.collection)

  private def baseUrl(request: Request): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }

  private def nextHref(request: Request, limit: Int, cursor: Option[String]) =
    cursor.map { c =>
      val url = baseUrl(request)
      val path = request.path
      s"""$url$path?linked_partitioning=1&limit=$limit&cursor=$c"""
    }

  private def hydrateUsers(session: UserSession, request: Request, limit: Int, reposts: Reposts): Future[Reposters] = {
    val url = baseUrl(request)
    val repostCounts =
      shouldLoadCountsFromReposts().flatMap {
        case true =>
          repostsClient.getRepostCountsByUrnWithFallback(session, reposts.reposts.toSet)
        case false => Future.value(Map.empty[Urn, Long])
      }
    val followCounts = followCountsClient.counts(session, reposts.reposts)
      .map {
      _.map { case value@FollowCounts(user, _, _) =>
        (user, value)
      }.toMap
    }
    val likeCounts =
      shouldLoadCountsFromLiebling().flatMap {
        case true =>
          lieblingClient.userTotalLikeCount(session, reposts.reposts)
            .map {
            _.map { case value@UserTotalLikes(user, _, _) =>
              (user, value)
            }.toMap
          }
        case false => Future.value(Map.empty[Urn, UserTotalLikes])
      }

    val hydratedUsers =
      okidokiClient.fetchRepostsUsersWithoutCounts(session,
                                                   reposts.reposts.toSet,
                                                   url)

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
      Reposters(users, nextHref(request, limit, reposts.nextCursor))
    }
  }
}
