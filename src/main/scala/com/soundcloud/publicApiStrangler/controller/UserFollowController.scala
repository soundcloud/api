package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.bff.{Json => BffJson}
import com.soundcloud.jvmkit.{UserSession, Geo}
import com.soundcloud.publicApiStrangler.clients.{Affiliation, FollowsClient, FollowsPage, PageInfo}
import com.soundcloud.publicApiStrangler.features.Rollout
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.scalakit.{UTF8, Urn}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, Years}
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.io.Source

class UserFollowController(userAuthentication: UserAuthentication,
                           fallback: DispatchToMothershipHandler,
                           okidoki: OkidokiClient,
                           follows: FollowsClient,
                           baseUrl: String,
                           rollout: Rollout)
  extends BffInjectionBasedController {

  val followRestrictions = {
    val source = Source.fromInputStream(getClass.getResourceAsStream("/user-follow-restrictions.json"), UTF8.name())
    try {
      Json.parse(source.mkString)
    } finally {
      source.close()
    }
  }

  val formatter = DateTimeFormat.forPattern("yyyy/M/d")

  // anonymous endpoints
  get("/users/:id/followings")(fetchFollowingsWithoutAuth)
  get("/users/:id/followings.json")(fetchFollowingsWithoutAuth)
  get("/users/:id/followers")(fetchFollowersWithoutAuth)
  get("/users/:id/followers.json")(fetchFollowersWithoutAuth)
  get("/users/:id/followers/followed_by/:other_id")(fetchMutualFollowers)
  get("/users/:id/followings/not_followed_by/:other_id")(fetchFollowingsNotFollowedBy)
  get("/users/:id/followings/common_to/:other_id")(fetchMutualFollowings)

  // logged-in only endpoints
  get("/me/followings")(fetchFollowings)
  get("/me/followers")(fetchMyFollowers)
  get("/me/followers/ids")(fetchMyFollowerIds)
  get("/me/followings/ids")(fetchMyFollowingIds)
  head("/me/followings/:id")(fallbackToMothership)
  post("/me/followings/:id")(fallbackToMothership)
  patch("/me/followings/:id")(fallbackToMothership)
  delete("/me/followings/:id")(fallbackToMothership)

  put("/me/followings/:id") { request =>
    userAuthentication.withLoggedInUser(request) {
      (session, userUrn) =>
        val restriction = findAgeRestriction(request.routeParams.get("id").get, session.getGeo)
        if (restriction.isEmpty) {
          fallbackToMothership(request)
        } else {
          findUserAge(session, userUrn).flatMap {
            case Some(userAge) => if (userAge < restriction.get) denyAgeRestricted(restriction.get) else fallbackToMothership(request)
            case _ => denyAgeUnknown
          }
        }
    }
  }

  private def fetchFollowingsNotFollowedBy(request: Request): Future[ResponseBuilder] = {
    fetchFromFollows(
      request,
      follows.followingsNotFollowedBy(
        _,
        Urn(s"soundcloud:users:${request.routeParams("id")}"),
        Urn(s"soundcloud:users:${request.routeParams("other_id")}"),
        _, _
      ),
      mapUsersToUsers,
      contacts,
      requireLogin = false
    )
  }

  private def fetchMutualFollowings(request: Request): Future[ResponseBuilder] = {
    fetchFromFollows(
      request,
      follows.mutualFollowings(
        _,
        Urn(s"soundcloud:users:${request.routeParams("id")}"),
        Urn(s"soundcloud:users:${request.routeParams("other_id")}"),
        _, _
      ),
      mapUsersToUsers,
      contacts,
      requireLogin = false
    )
  }

  private def fetchMutualFollowers(request: Request): Future[ResponseBuilder] = {
    fetchFromFollows(
      request,
      follows.mutualFollowers(
        _,
        Urn(s"soundcloud:users:${request.routeParams("id")}"),
        Urn(s"soundcloud:users:${request.routeParams("other_id")}"),
        _, _
      ),
      mapUsersToUsers,
      fans,
      requireLogin = false
    )
  }

  private def fetchFollowersWithoutAuth(request: Request): Future[ResponseBuilder] = fetchFromFollows(request, follows.followers, mapUsersToUsers, fans, requireLogin = false)

  private def fetchFollowingsWithoutAuth(request: Request): Future[ResponseBuilder] = fetchFromFollows(request, follows.followings, mapUsersToUsers, contacts, requireLogin = false)

  private def fetchMyFollowers(request: Request): Future[ResponseBuilder] = fetchFromFollows(request, follows.followers, mapUsersToUsers, fans, requireLogin = true)

  private def fetchFollowings(request: Request) = fetchFromFollows(request, follows.followings, mapUsersToUsers, contacts, requireLogin = true)

  private def fetchMyFollowingIds(request: Request) = fetchFromFollows(request, follows.followings, userIds, contacts, requireLogin = true)

  private def fetchMyFollowerIds(request: Request) = fetchFromFollows(request, follows.followers, userIds, fans, requireLogin = true)

  private def mapUsersToUsers(users: List[User]): List[Any] = users

  private def userIds(users: List[User]): List[Any] = users.map(u => u.id)

  private def fans(affiliations: Seq[Affiliation]): Seq[Urn] = affiliations.map(_.user)

  private def contacts(affiliations: Seq[Affiliation]): Seq[Urn] = affiliations.map(_.target)

  private def pageSizeParam(request: Request) = {
    request.params.get("limit")
      .orElse(request.params.get("page_size"))
      .map(_.toInt).getOrElse(50)
  }

  private def cursorParam(request: Request) = request.params.get("cursor")

  private def fetchFromFollows(request: Request,
                               fetchFunction: (UserSession, Int, Option[String]) => Future[FollowsPage],
                               mapUsers: List[User] => List[Any] = mapUsersToUsers,
                               users: Seq[Affiliation] => Seq[Urn],
                               requireLogin: Boolean): Future[ResponseBuilder] = {
    authenticateIfNeeded(request, requireLogin) { (session: UserSession) =>
      if(rollingOutReads(session)) {
        for {
          affiliations <- fetchFunction(session, pageSizeParam(request), cursorParam(request))
          urns = users(affiliations.values)
          users <- fetchUsers(session, urns.toSet)
        } yield {
          render.json(Map(
            "collection" -> mapUsers(users),
            "next_href" -> nextHref(baseUrl, request.request.path, affiliations.page, request.params)
          ))
        }
      } else {
        fallbackToMothership(request)
      }
    }
  }

  private def authenticateIfNeeded(request: Request, requireLogin: Boolean)(withSession: UserSession => Future[ResponseBuilder]) = {
    if(requireLogin) {
      userAuthentication.withLoggedInUser(request) { (loggedIn, _) => withSession(loggedIn) }
    } else {
      userAuthentication.withUserSession(request)(withSession)
    }
  }

  private def rollingOutReads(session: UserSession): Boolean = {
    rollout.isActiveForId("follows-reads", session.getUser)
  }

  private def nextHref(baseUrl: String, path: String, pageInfo: PageInfo, requestParams: Map[String, String]): Option[String] = {
    pageInfo.lastId.map { nextId =>
      val params = requestParams ++ Map("cursor" -> nextId, "page_size" -> pageInfo.size) -- Seq("limit")
      baseUrl + path + "?" + params.map { case(k, v) => s"$k=$v" }.mkString("&")
    }
  }

  private def fetchUsers(session: UserSession, urns: Set[Urn]): Future[List[User]] = {
    val context = new MappingContext(session)
    okidoki.fetch(session, urns).map { users =>
      users.map(user => new User(user, baseUrl)(context))
    }
  }

  private def findAgeRestriction(userId: String, geo: Geo): Option[Long] = {
    val restrictions = followRestrictions \ userId \ "age"
    (restrictions \ geo.getCountryCode).asOpt[Long].orElse((restrictions \ "*").asOpt[Long])
  }

  private def findUserAge(session: UserSession, userUrn: Urn): Future[Option[Long]] = {
    okidoki.fetch(session, Set(userUrn)).map {
      case user :: xs => (user \ "date_of_birth").asOpt[String].map(currentAge)
      case _ => None
    }
  }

  private def currentAge(dateOfBirth: String): Long = {
    val dob = formatter.parseLocalDate(dateOfBirth)
    Years.yearsBetween(dob, new LocalDate()).getYears
  }

  private def denyAgeRestricted(age: Long): Future[ResponseBuilder] = {
    render.json(Map("errors" -> Seq(Map("error_message" -> "DENY_AGE_RESTRICTED", "age" -> age))))
      .status(Status.Forbidden.getCode)
      .toFuture
  }

  private def denyAgeUnknown: Future[ResponseBuilder] = {
    render.json(Map("errors" -> Seq(Map("error_message" -> "DENY_AGE_UNKNOWN"))))
      .status(Status.Forbidden.getCode)
      .toFuture
  }

  private def fallbackToMothership(request: Request): Future[ResponseBuilder] = {
    fallback.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).map { response =>
      val headers = response.headers().entries().map(e => e.getKey -> e.getValue).toMap
      new ResponseBuilder().status(response.getStatusCode()).body(response.getContentString()).headers(headers)
    }
  }
}
