package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.follows.client._
import com.soundcloud.follows.client.representation._
import com.soundcloud.jvmkit.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.scalakit.{Geo, LoggedInUserSession, UTF8, Urn, UserSession}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, Years}
import play.api.libs.json.{JsObject, Json}

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
  get("/users/:id/followers/recent")(fetchFollowersWithoutAuth)
  get("/users/:id/followers/ids")(fetchFollowerIdsWithoutAuth)
  get("/users/:id/followers/ids.json")(fetchFollowerIdsWithoutAuth)
  get("/users/:id/followings/ids")(fetchFollowingIdsWithoutAuth)
  get("/users/:id/followings/ids.json")(fetchFollowingIdsWithoutAuth)
  get("/users/:id/followers/followed_by/:other_id")(fetchFollowersFollowed)
  get("/users/:id/followings/not_followed_by/:other_id")(fetchFollowingsNotFollowedBy)
  get("/users/:id/followings/common_to/:other_id")(fetchMutualFollowings)
  get("/users/:id/followers/:other_id")(fetchPossibleFollowerWithoutAuth)
  get("/users/:id/followers/:other_id.json")(fetchPossibleFollowerWithoutAuth)
  get("/users/:id/followings/:other_id")(fetchPossibleFollowingWithoutAuth)
  get("/users/:id/followings/:other_id.json")(fetchPossibleFollowingWithoutAuth)

  // logged-in only endpoints
  get("/me/followings")(fetchFollowings)
  get("/me/followings.json")(fetchFollowings)
  get("/me/followers")(fetchMyFollowers)
  get("/me/followers.json")(fetchMyFollowers)
  get("/me/followers/recent")(fetchMyFollowers)
  get("/me/followers/ids")(fetchMyFollowerIds)
  get("/me/followers/ids.json")(fetchMyFollowerIds)
  get("/me/followings/ids")(fetchMyFollowingIds)
  get("/me/followings/ids.json")(fetchMyFollowingIds)
  get("/me/followings/tracks")(fallbackToMothership)
  get("/me/followers/:other_id")(fetchPossibleFollower)
  get("/me/followers/:other_id.json")(fetchPossibleFollower)
  get("/me/followings/:other_id")(fetchPossibleFollowing)
  get("/me/followings/:other_id.json")(fetchPossibleFollowing)
  head("/me/followings/:other_id")(fallbackToMothership)
  post("/me/followings/:other_id")(fallbackToMothership)
  patch("/me/followings/:other_id")(fallbackToMothership)
  delete("/me/followings/:other_id")(fallbackToMothership)

  put("/me/followings/:other_id") { request =>
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      rollingOutWrites(userUrn).flatMap{
        case true =>
          val user = Urn("soundcloud:users:" + request.routeParams.get("other_id").get)
          follows.follow(session, user).flatMap {
            case success: FollowSuccessful => renderFollow(session, success)
            case error: FollowFailed => renderFollowFailed(error, session, user)
          }
        case false =>
          val restriction = findAgeRestriction(request.routeParams.get("other_id").get, session.getGeo)
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
  }

  private def renderFollowFailed(error: FollowFailed, session: UserSession, userUrn: Urn): Future[ResponseBuilder] = {
    fetchUserAgeIfNeeded(error, session, userUrn).map { userAge =>
      render.status(error.status).typedJson(
        Json.obj(
          "errors" -> Json.arr(
            fieldsWithAgeRestrictionHack(error, userAge)
          )
        )
      )
    }
  }

  private def fetchUserAgeIfNeeded(fail: FollowFailed, session: UserSession, userUrn: Urn): Future[Option[Int]] = {
    if(fail.isAgeRestricted) {
      findUserAge(session, userUrn)
    } else {
      Future { None }
    }
  }

  // for some reason, the strangler response now includes an "age" field on the error object.
  private def fieldsWithAgeRestrictionHack(error: FollowFailed, userAge: Option[Int]): JsObject = {
    error.name match {
      case _ if userAge.isDefined && error.isAgeRestricted =>
        Json.obj(
          "error_message" -> "DENY_AGE_RESTRICTED",
          "age" -> userAge.get
        )
      case _ if error.isAgeUnknown | error.isAgeRestricted => // couldn't fetch the age thing or age is unknown
        Json.obj(
          "error_message" -> "DENY_AGE_UNKNOWN"
        )
      case other =>
        Json.obj("error_message" -> error.message)
    }
  }

  private def renderFollow(session: LoggedInUserSession, follow: FollowSuccessful): Future[ResponseBuilder] = {
    fetchUsers(session, Set(follow.target)).map { users =>
      render.json(
        users.headOption
      )
    }
  }

  private def fetchFollowingsNotFollowedBy(request: Request): Future[ResponseBuilder] = {
    fetchUrns(
      request,
      follows.followingsNotFollowedBy(
        _,
        _,
        Urn(s"soundcloud:users:${request.routeParams("other_id")}")
      )
    )
  }

  private def fetchMutualFollowings(request: Request): Future[ResponseBuilder] = {
    fetchUrns(
      request,
      follows.mutualFollowings(
        _,
        _,
        Urn(s"soundcloud:users:${request.routeParams("other_id")}")
      )
    )
  }

  private def fetchFollowersFollowed(request: Request): Future[ResponseBuilder] = {
    fetchUrns(
      request,
      follows.followersFollowedBy(
        _,
        _,
        Urn(s"soundcloud:users:${request.routeParams("other_id")}")
      )
    )
  }

  private def fetchFollowersWithoutAuth(request: Request): Future[ResponseBuilder] = fetchPage(request, follows.followers, mapUsersToUsers, fans, requireLogin = false)

  private def fetchFollowingsWithoutAuth(request: Request): Future[ResponseBuilder] = fetchPage(request, follows.followings, mapUsersToUsers, contacts, requireLogin = false)

  private def fetchMyFollowers(request: Request): Future[ResponseBuilder] = fetchPage(request, follows.followers, mapUsersToUsers, fans, requireLogin = true)

  private def fetchFollowings(request: Request) = fetchPage(request, follows.followings, mapUsersToUsers, contacts, requireLogin = true)

  private def fetchFollowingIdsWithoutAuth(request: Request) = fetchPage(request, follows.followings, userIds, contacts, requireLogin = false)

  private def fetchFollowerIdsWithoutAuth(request: Request) = fetchPage(request, follows.followers, userIds, fans, requireLogin = false)

  private def fetchMyFollowingIds(request: Request) = fetchPage(request, follows.followings, userIds, contacts, requireLogin = true)

  private def fetchMyFollowerIds(request: Request) = fetchPage(request, follows.followers, userIds, fans, requireLogin = true)

  private def fetchPossibleFollowingWithoutAuth(request: Request) = fetchUser(request, follows.filterFollowings, requireLogin = false)

  private def fetchPossibleFollowerWithoutAuth(request: Request) = fetchUser(request, follows.filterFollowers, requireLogin = false)

  private def fetchPossibleFollowing(request: Request) = fetchUser(request, follows.filterFollowings, requireLogin = true)

  private def fetchPossibleFollower(request: Request) = fetchUser(request, follows.filterFollowers, requireLogin = true)

  private def mapUsersToUsers(users: List[User]): List[Any] = users

  private def userIds(users: List[User]): List[Any] = users.map(u => u.id)

  private def fans(affiliations: Seq[Following]): Seq[Urn] = affiliations.map(_.user)

  private def contacts(affiliations: Seq[Following]): Seq[Urn] = affiliations.map(_.target)

  private def pageSizeParam(request: Request) = {
    request.params.get("limit")
      .orElse(request.params.get("page_size"))
      .map(_.toInt).getOrElse(50)
  }

  private def cursorParam(request: Request) = request.params.get("cursor")

  private def fetchUrns(request: Request,
                        fetchFunction: (UserSession, Urn) => Future[Option[UserUrns]]): Future[ResponseBuilder] = {
    authenticateIfNeeded(request, requireLogin = false) { (session: UserSession, userToFetch: Urn) =>
      rollingOutReads(userToFetch).flatMap {
        case false => fallbackToMothership(request)
        case true => for {
          responseOption <- fetchFunction(session, userToFetch)
          urns = responseOption.map(_.urns.toSet).getOrElse(Set.empty)
          users <- fetchUsers(session, urns)
        } yield {
          responseOption.map { _ =>
            render.json(Map(
              "collection" -> mapUsersToUsers(users)
            ))
          }.getOrElse(render.serviceUnavailable)
        }
      }
    }
  }

  private def fetchPage(request: Request,
                         fetchFunction: (UserSession, Urn, Option[String], Int) => Future[Option[FollowingsPage]],
                         mapUsers: List[User] => List[Any] = mapUsersToUsers,
                         users: Seq[Following] => Seq[Urn],
                         requireLogin: Boolean): Future[ResponseBuilder] = {
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, userToFetch: Urn) =>
      rollingOutReads(userToFetch).flatMap {
        case false => fallbackToMothership(request)
        case true => for {
          affiliationsOption <- fetchFunction(session, userToFetch, cursorParam(request), pageSizeParam(request))
          urns = affiliationsOption.map(page => users(page.followings).toSet).getOrElse(Set.empty)
          users <- fetchUsers(session, urns)
        } yield {
          affiliationsOption.map { affiliations =>
            render.json(Map(
              "collection" -> mapUsers(users),
              "next_href" -> nextHref(baseUrl, request.request.path, affiliations.next, request.params)
            ))
          }.getOrElse(render.serviceUnavailable)
        }
      }
    }
  }

  private def fetchUser(request: Request,
                        filteringFunction: (UserSession, Urn, Seq[Urn]) => Future[Option[FilteredUserUrns]],
                        requireLogin: Boolean): Future[ResponseBuilder] =
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, loggedInUser: Urn) =>
      val userId = request.routeParams.get("other_id").get
      val user = Urn("soundcloud:users:" + userId)

      rollingOutReads(loggedInUser).flatMap {
        case false => fallbackToMothership(request)
        case true => for {
          filteredOption <- filteringFunction(session, loggedInUser, Seq(user))
          urns = filteredOption.map(_.included).getOrElse(Set.empty)
          users <- fetchUsers(session, urns)
        } yield {
          filteredOption.map { _ =>
            if (users.nonEmpty) {
              render
                .status(Status.SeeOther.getCode)
                .header("Location", s"$baseUrl/users/${userId}")
                .json(users.head)
            } else {
              render.notFound
            }
          }.getOrElse(render.serviceUnavailable)
        }
      }
    }

  private def authenticateIfNeeded(request: Request, requireLogin: Boolean)(withSession: (UserSession, Urn) => Future[ResponseBuilder]) = {
    if(requireLogin) {
      userAuthentication.withLoggedInUser(request) { (loggedIn, _) => withSession(loggedIn, loggedIn.getUser) }
    } else {
      userAuthentication.withUserSession(request){ s => withSession(s, Urn(s"soundcloud:users:${request.routeParams("id")}")) }
    }
  }

  private def rollingOutReads(userToFetch: Urn): Future[Boolean] = rollout.isActiveForUrn(BasicRolloutFeature("follows-reads"), userToFetch)

  private def rollingOutWrites(userUrn: Urn): Future[Boolean] = rollout.isActiveForUrn(BasicRolloutFeature("follows-writes"), userUrn)

  private def nextHref(baseUrl: String, path: String, next: Option[Pagination], requestParams: Map[String, String]): Option[String] = {
    next.map { pagination =>
      val params = requestParams ++ Map("cursor" -> pagination.cursor, "page_size" -> pagination.page_size) -- Seq("limit")
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

  private def findUserAge(session: UserSession, userUrn: Urn): Future[Option[Int]] = {
    okidoki.fetch(session, Set(userUrn)).map {
      case user :: xs => (user \ "date_of_birth").asOpt[String].map(currentAge)
      case _ => None
    }
  }

  private def currentAge(dateOfBirth: String): Int = {
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
