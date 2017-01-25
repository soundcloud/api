package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser.writes
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
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
      if(limit <= 200)
        repostsClient.reposters(
          session,
          new Urn(s"""soundcloud:$repostableType:${request.routeParams("id")}"""),
          limit
        ).flatMap(hydrateUsers(session, baseUrl(request), _)).map(respond)
      else Future.value(render.badRequest)
    }

  private def respond(users: List[RepostsUser]): ResponseBuilder =
    render.json(users)

  private def baseUrl(request: Request): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }

  private def hydrateUsers(session: UserSession, baseUrl: String, users: List[Urn]): Future[List[RepostsUser]] = {
    val repostCounts =
      shouldLoadCountsFromReposts().flatMap {
        case true =>
          repostsClient.getRepostCountsByUrnWithFallback(session, users.toSet)
        case false => Future.value(Map.empty[Urn, Long])
      }
    val followCounts = followCountsClient.counts(session, users)
      .map {
      _.map { case value@FollowCounts(user, _, _) =>
        (user, value)
      }.toMap
    }
    val likeCounts =
      shouldLoadCountsFromLiebling().flatMap {
        case true =>
          lieblingClient.userTotalLikeCount(session, users)
            .map {
            _.map { case value@UserTotalLikes(user, _, _) =>
              (user, value)
            }.toMap
          }
        case false => Future.value(Map.empty[Urn, UserTotalLikes])
      }

    val hydratedUsers =
      okidokiClient.fetchRepostsUsersWithoutCounts(session, users.toSet, baseUrl)

    for {
      (countReposts,
       countFollows,
       countLikes,
       fullUsers) <- Future.join(repostCounts,
                                 followCounts,
                                 likeCounts,
                                 hydratedUsers)
    } yield {
      fullUsers.map { case user =>
        val followsCount = countFollows.get(user.urn)
        val repostsCount = countReposts.get(user.urn)
        val likesCount = countLikes.get(user.urn)

        user.copy(maybeFollowCounts = followsCount,
                  maybeRepostsCount = repostsCount,
                  maybeLikesCount = likesCount)(user.context)
      }
    }
  }
}
