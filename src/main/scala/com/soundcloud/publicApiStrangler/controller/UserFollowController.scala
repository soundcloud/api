package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.follows._
import com.soundcloud.publicApiStrangler.client.follows.representation._
import com.soundcloud.publicApiStrangler.client.follows.representation.follow._
import com.soundcloud.publicApiStrangler.client.follows.representation.unfollow.{NotFollowing, UnfollowSuccessful, UnknownError => UnfollowUnknownError, UserAsTarget => UnfollowUserAsTarget, UserNotFound => UnfollowUserNotFound}
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, Years}
import play.api.libs.json._

class UserFollowController(userAuthentication: UserAuthentication,
                           fallback: DispatchToMothershipHandler,
                           okidoki: OkidokiClient,
                           follows: FollowsClient,
                           followCountsClient: FollowCountsClient,
                           baseUrl: String)
  extends BffInjectionBasedController {

  val formatter = DateTimeFormat.forPattern("yyyy/M/d")

  // anonymous endpoints

  get("/users/:id/followings")(fetchFollowingsWithoutAuth)
  get("/users/:id/followings.json")(fetchFollowingsWithoutAuth)

  get("/users/:id/followers")(fetchFollowersWithoutAuth)
  get("/users/:id/followers.json")(fetchFollowersWithoutAuth)

  get("/users/:id/followers/recent")(fetchFollowersWithoutAuth)
  get("/users/:id/followers/recent.json")(fetchFollowersWithoutAuth)

  get("/users/:id/followers/ids")(fetchFollowerIdsWithoutAuth)
  get("/users/:id/followers/ids.json")(fetchFollowerIdsWithoutAuth)

  get("/users/:id/followings/ids")(fetchFollowingIdsWithoutAuth)
  get("/users/:id/followings/ids.json")(fetchFollowingIdsWithoutAuth)

  get("/users/:id/followers/followed_by/:other_id")(fetchFollowersFollowed)
  get("/users/:id/followers/followed_by/:other_id.json")(fetchFollowersFollowed)

  get("/users/:id/followings/not_followed_by/:other_id")(fetchFollowingsNotFollowedBy)
  get("/users/:id/followings/not_followed_by/:other_id.json")(fetchFollowingsNotFollowedBy)

  get("/users/:id/followings/common_to/:other_id")(fetchMutualFollowings)
  get("/users/:id/followings/common_to/:other_id.json")(fetchMutualFollowings)

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
  get("/me/followers/recent.json")(fetchMyFollowers)

  get("/me/followers/ids")(fetchMyFollowerIds)
  get("/me/followers/ids.json")(fetchMyFollowerIds)

  get("/me/followings/ids")(fetchMyFollowingIds)
  get("/me/followings/ids.json")(fetchMyFollowingIds)

  get("/me/followers/:other_id")(fetchPossibleFollower)
  get("/me/followers/:other_id.json")(fetchPossibleFollower)

  get("/me/followings/:other_id")(fetchPossibleFollowing)
  get("/me/followings/:other_id.json")(fetchPossibleFollowing)

  head("/me/followings/:other_id")(fallback.dispatch)
  head("/me/followings/:other_id.json")(fallback.dispatch)

  post("/me/followings/:other_id")(follow)
  post("/me/followings/:other_id.json")(follow)
  put("/me/followings/:other_id")(follow)
  put("/me/followings/:other_id.json")(follow)

  delete("/me/followings/:other_id")(unfollow)
  delete("/me/followings/:other_id.json")(unfollow)

  // Legacy logged-in endpoints

  get("/v1/me/followings")(fetchFollowings)
  get("/v1/me/followings.json")(fetchFollowings)

  get("/v1/me/followers")(fetchMyFollowers)
  get("/v1/me/followers.json")(fetchMyFollowers)

  get("/v1/me/followers/recent")(fetchMyFollowers)
  get("/v1/me/followers/recent.json")(fetchMyFollowers)

  get("/v1/me/followers/ids")(fetchMyFollowerIds)
  get("/v1/me/followers/ids.json")(fetchMyFollowerIds)

  get("/v1/me/followings/ids")(fetchMyFollowingIds)
  get("/v1/me/followings/ids.json")(fetchMyFollowingIds)

  get("/v1/me/followers/:other_id")(fetchPossibleFollower)
  get("/v1/me/followers/:other_id.json")(fetchPossibleFollower)

  get("/v1/me/followings/:other_id")(fetchPossibleFollowing)
  get("/v1/me/followings/:other_id.json")(fetchPossibleFollowing)

  head("/v1/me/followings/:other_id")(fallback.dispatch)
  head("/v1/me/followings/:other_id.json")(fallback.dispatch)

  post("/v1/me/followings/:other_id")(follow)
  post("/v1/me/followings/:other_id.json")(follow)
  put("/v1/me/followings/:other_id")(follow)
  put("/v1/me/followings/:other_id.json")(follow)

  delete("/v1/me/followings/:other_id")(unfollow)
  delete("/v1/me/followings/:other_id.json")(unfollow)

  private def follow(request: Request): Future[ResponseBuilder] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val user = new Urn("soundcloud:users:" + request.routeParams.get("other_id").get)
      follows.follow(session, user).flatMap {
        case _: FollowingCreated => renderFollow(session, user)
        case AlreadyFollowing => renderStatus(Status.Ok)
        case UserNotFound => renderError(Status.NotFound)
        case _: SpamBlocked => renderError(Status.TooManyRequests)
        case MaxFollowingsReached => renderError(Status.UnprocessableEntity)
        case BlockedByTarget => renderError(Status.Forbidden)
        case UserAsTarget => renderError(Status.BadRequest)
        case AgeRestrictedUser =>  findUserAge(session, userUrn).flatMap {
          case Some(userAge) => denyAgeRestricted(userAge)
          case _ => denyAgeUnknown
        }
        case AgeUnknownUser => denyAgeUnknown
        case _: UnknownError | BulkFollowFailed(_) => renderError(Status.InternalServerError)
      }
    }
  }

  private def unfollow(request: Request): Future[ResponseBuilder] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val user = new Urn("soundcloud:users:" + request.routeParams.get("other_id").get)
      follows.unfollow(session, user).flatMap {
        case UnfollowSuccessful => renderStatus(Status.Ok)
        case UnfollowUserNotFound => renderError(Status.NotFound)
        case UnfollowUserAsTarget | NotFollowing => renderError(Status.UnprocessableEntity)
        case _: UnfollowUnknownError => renderError(Status.InternalServerError)
      }
    }
  }

  private def renderFollow(session: LoggedInUserSession, target: Urn): Future[ResponseBuilder] = {
    fetchUsers(session, Set(target)).map { users =>
      render.json(users.headOption).status(Status.Created.code)
    }
  }

  private def renderStatus(status: Status) =
    render.json(Json.obj("status" -> s"$status - ${status.reason}"))
      .status(status.code)
      .toFuture

  private def renderError(status: Status) =
    render.json(Json.obj("errors" -> Seq(Map("error_message" -> s"${status.code} - ${status.reason}"))))
      .status(status.code)
      .toFuture

  private def fetchFollowingsNotFollowedBy(request: Request): Future[ResponseBuilder] = {
    fetchUrns(
      request,
      follows.followingsNotFollowedBy(
        _,
        _,
        new Urn(s"soundcloud:users:${request.routeParams("other_id")}")
      )
    )
  }

  private def fetchMutualFollowings(request: Request): Future[ResponseBuilder] = {
    fetchUrns(
      request,
      follows.mutualFollowings(
        _,
        _,
        new Urn(s"soundcloud:users:${request.routeParams("other_id")}")
      )
    )
  }

  private def fetchFollowersFollowed(request: Request): Future[ResponseBuilder] = {
    fetchUrns(
      request,
      follows.followersFollowedBy(
        _,
        _,
        new Urn(s"soundcloud:users:${request.routeParams("other_id")}")
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

  private def mapUsersToUsers(users: List[User]): List[JsValue] = Json.toJson(users).as[List[JsValue]]

  private def userIds(users: List[User]): List[JsValue] = Json.toJson(users.map(u => u.id)).as[List[JsValue]]

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
      for {
        responseOption <- fetchFunction(session, userToFetch)
        urns = responseOption.map(_.urns.toSet).getOrElse(Set.empty)
        users <- fetchUsers(session, urns)
      } yield {
        responseOption.map { _ =>
          render.json(Json.obj("collection" -> mapUsersToUsers(users)))
        }.getOrElse(render.serviceUnavailable)
      }
    }
  }

  private def fetchPage[T](request: Request,
                           fetchFunction: (UserSession, Urn, Option[String], Int) => Future[Option[FollowingsPage]],
                           mapUsers: List[User] => List[JsValue] = mapUsersToUsers,
                           users: Seq[Following] => Seq[Urn],
                           requireLogin: Boolean): Future[ResponseBuilder] = {
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, userToFetch: Urn) =>
      for {
        affiliationsOption <- fetchFunction(session, userToFetch, cursorParam(request), pageSizeParam(request))
        urns = affiliationsOption.map(page => users(page.followings).toSet).getOrElse(Set.empty)
        users <- fetchUsers(session, urns)
      } yield {
        affiliationsOption.map { affiliations =>
          render.json(Json.obj(
            "collection" -> mapUsers(users),
            "next_href" -> nextHref(baseUrl, request.request.path, affiliations.next, request.params)))
        }.getOrElse(render.serviceUnavailable)
      }
    }
  }

  private def fetchUser(request: Request,
                        filteringFunction: (UserSession, Urn, Seq[Urn]) => Future[Option[FilteredUserUrns]],
                        requireLogin: Boolean): Future[ResponseBuilder] =
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, loggedInUser: Urn) =>
      val userId = request.routeParams.get("other_id").get
      val user = new Urn("soundcloud:users:" + userId)

      for {
        filteredOption <- filteringFunction(session, loggedInUser, Seq(user))
        urns = filteredOption.map(_.included).getOrElse(Set.empty)
        users <- fetchUsers(session, urns)
      } yield {
        filteredOption.map { _ =>
          if (users.nonEmpty) {
            render
              .status(Status.SeeOther.code)
              .header("Location", s"$baseUrl/users/$userId")
              .json(users.head)
          } else {
            render.notFound
          }
        }.getOrElse(render.serviceUnavailable)
      }
    }

  private def authenticateIfNeeded(request: Request, requireLogin: Boolean)(withSession: (UserSession, Urn) => Future[ResponseBuilder]) = {
    if (requireLogin) {
      userAuthentication.withLoggedInUser(request) { (loggedIn, _) => withSession(loggedIn, loggedIn.getUser) }
    } else {
      userAuthentication.withUserSession(request) { s => withSession(s, new Urn(s"soundcloud:users:${request.routeParams("id")}")) }
    }
  }

  private def nextHref(baseUrl: String, path: String, next: Option[Pagination], requestParams: Map[String, String]): Option[String] = {
    next.map { pagination =>
      val params = requestParams ++ Map("cursor" -> pagination.cursor, "page_size" -> pagination.page_size) -- Seq("limit")
      baseUrl + path + "?" + params.map { case (k, v) => s"$k=$v" }.mkString("&")
    }
  }

  private def fetchUsers(session: UserSession, urns: Set[Urn]): Future[List[User]] = {
    val context = new MappingContext(session)
    for {
      (users, followCountsMap) <- Future.join(
        okidoki.fetch(session, urns),
        followCountsClient
          .counts(session, urns.toSeq)
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap)
      )
    } yield {
      users.map { user =>
        val followCounts = followCountsMap.get(new Urn((user \ "self" \ "urn").as[String]))
        new User(user, baseUrl, followCounts)(context)
      }
    }
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
    val errors = JsArray(Seq(JsObject(Seq(
      "error_message" -> JsString("DENY_AGE_RESTRICTED"),
      "age" -> JsNumber(age)
    ))))
    render.json(JsObject(Seq("errors" -> errors)))
      .status(Status.Forbidden.code)
      .toFuture
  }

  private def denyAgeUnknown: Future[ResponseBuilder] = {
    val errors = JsArray(Seq(JsObject(Seq(
      "error_message" -> JsString("DENY_AGE_UNKNOWN")
    ))))
    render.json(JsObject(Seq("errors" -> errors)))
      .status(Status.Forbidden.code)
      .toFuture
  }
}
