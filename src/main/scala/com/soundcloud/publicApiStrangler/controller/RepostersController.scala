package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.twitter.util.Future

class RepostersController(userAuthentication: UserAuthentication,
                          repostsClient: RepostsClient,
                          followCountsClient: FollowCountsClient,
                          shouldLoadCountsFromReposts: () => Future[Boolean])
    extends BffInjectionBasedController {

  get("/e1/tracks/:id/reposters")(reposters("tracks"))
  get("/e1/tracks/:id/reposters.json")(reposters("tracks"))

  get("/e1/playlists/:id/reposters")(reposters("playlists"))
  get("/e1/playlists/:id/reposters.json")(reposters("playlists"))

  private def reposters(repostableType: String)(request: Request): Future[ResponseBuilder] =
    userAuthentication.withUserSession(request) { session =>
      repostsClient.reposters(
        session,
        new Urn(s"""soundcloud:$repostableType:${request.routeParams("id")}"""),
        baseUrl(request)).flatMap(enrichReposts(session, _)).map(respond)
    }

  private def respond(users: List[User]): ResponseBuilder =
    render.json(users)

  private def baseUrl(request: Request): String = {
    // default means that request is coming from a dev environment
    val protocol = request.headerMap.getOrElse("X-Forwarded-Proto", "http")
    s"$protocol://${request.host.get}"
  }

  private def enrichReposts(session: UserSession, users: List[User]): Future[List[User]] = {
    val userUrns = users.map(_.urn)
    val repostCounts =
      shouldLoadCountsFromReposts().flatMap {
        case true =>
          repostsClient.getRepostCountsByUrnWithFallback(session, userUrns.toSet)
        case false => Future.value(Map.empty[Urn, Long])
      }
    val followCounts = followCountsClient.counts(session, userUrns)
      .map {
      _.map { case value@FollowCounts(user, _, _) =>
        (user, value)
      }.toMap
    }
    for {
      (countReposts, countFollows) <- Future.join(repostCounts, followCounts)
    } yield {
      users.toList.map { case user =>
        val followCounts = countFollows.get(user.urn)
        val repostsCount = countReposts.get(user.urn)
        user.copy(maybeFollowCounts = followCounts,
                  maybeRepostsCount = repostsCount)(user.context)
      }
    }
  }

}
