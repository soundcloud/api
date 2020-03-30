package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{LoggedInUserSession, UserSession}
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.follows._
import com.soundcloud.publicApiStrangler.client.follows.representation._
import com.soundcloud.publicApiStrangler.client.follows.representation.follow._
import com.soundcloud.publicApiStrangler.client.follows.representation.unfollow.{
  NotFollowing,
  UnfollowSuccessful,
  UnknownError => UnfollowUnknownError,
  UserAsTarget => UnfollowUserAsTarget,
  UserNotFound => UnfollowUserNotFound
}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.User
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, Years}
import play.api.libs.json._

class UserFollowHandler(
    userAuthentication: UserAuthentication,
    okidoki: OkidokiClient,
    follows: FollowsClient,
    followCountsClient: FollowCountsClient,
    repostsClient: RepostsClient,
    baseUrl: String
) {
  val formatter = DateTimeFormat.forPattern("yyyy/M/d")

  def follow(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val user = Urn("soundcloud", "users", request.routeParams.get("other_id").get)
      follows.follow(session, user).flatMap {
        case _: FollowingCreated => renderFollow(session, user)
        case AlreadyFollowing => renderStatus(Status.Ok)
        case UserNotFound => renderError(Status.NotFound)
        case SpamBlocked => renderError(Status.TooManyRequests)
        case MaxFollowingsReached => renderError(Status.UnprocessableEntity)
        case BlockedByTarget => renderError(Status.Forbidden)
        case UserAsTarget => renderError(Status.BadRequest)
        case AgeRestrictedUser =>
          findUserAge(session, userUrn).flatMap {
            case Some(userAge) => denyAgeRestricted(userAge)
            case _ => denyAgeUnknown
          }
        case AgeUnknownUser => denyAgeUnknown
        case _: UnknownError | BulkFollowFailed(_) => renderError(Status.InternalServerError)
      }
    }
  }

  def unfollow(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      val user = Urn("soundcloud", "users", request.routeParams.get("other_id").get)
      follows.unfollow(session, user).flatMap {
        case UnfollowSuccessful => renderStatus(Status.Ok)
        case UnfollowUserNotFound => renderError(Status.NotFound)
        case UnfollowUserAsTarget | NotFollowing => renderError(Status.UnprocessableEntity)
        case _: UnfollowUnknownError => renderError(Status.InternalServerError)
      }
    }
  }

  private def renderFollow(session: LoggedInUserSession, target: Urn): Future[Response] = {
    fetchUsers(session, Set(target)).map { users =>
      JsonResponseBuilder.created(Json.stringify(Json.toJson(users.headOption)))
    }
  }

  private def renderStatus(status: Status): Future[Response] =
    Future.value(
      JsonResponseBuilder(
        status,
        body = Json.stringify(Json.obj("status" -> s"$status - ${status.reason}"))
      ).build
    )

  private def renderError(status: Status): Future[Response] =
    Future.value(
      JsonResponseBuilder(
        status,
        body = Json.stringify(Json.obj("errors" -> Seq(Map("error_message" -> s"${status.code} - ${status.reason}"))))
      ).build
    )

  def fetchFollowersWithoutAuth(request: HandlerRequest): Future[Response] =
    fetchPage(request, follows.followers, mapUsersToUsers, fans, requireLogin = false)

  def fetchFollowingsWithoutAuth(request: HandlerRequest): Future[Response] =
    fetchPage(request, follows.followings, mapUsersToUsers, contacts, requireLogin = false)

  def fetchMyFollowers(request: HandlerRequest): Future[Response] =
    fetchPage(request, follows.followers, mapUsersToUsers, fans, requireLogin = true)

  def fetchFollowings(request: HandlerRequest) =
    fetchPage(request, follows.followings, mapUsersToUsers, contacts, requireLogin = true)

  def fetchFollowingIdsWithoutAuth(request: HandlerRequest) =
    fetchPage(request, follows.followings, userIds, contacts, requireLogin = false)

  def fetchMyFollowingIds(request: HandlerRequest) =
    fetchPage(request, follows.followings, userIds, contacts, requireLogin = true)

  def fetchMyFollowerIds(request: HandlerRequest) =
    fetchPage(request, follows.followers, userIds, fans, requireLogin = true)

  def fetchPossibleFollowingWithoutAuth(request: HandlerRequest) =
    fetchUser(request, follows.filterFollowings, requireLogin = false)

  def fetchPossibleFollowerWithoutAuth(request: HandlerRequest) =
    fetchUser(request, follows.filterFollowers, requireLogin = false)

  def fetchPossibleFollowing(request: HandlerRequest) =
    fetchUser(request, follows.filterFollowings, requireLogin = true)

  def fetchPossibleFollower(request: HandlerRequest) = fetchUser(request, follows.filterFollowers, requireLogin = true)

  private def mapUsersToUsers(users: List[User]): List[JsValue] = Json.toJson(users).as[List[JsValue]]

  private def userIds(users: List[User]): List[JsValue] = Json.toJson(users.map(u => u.id)).as[List[JsValue]]

  private def fans(affiliations: Seq[Following]): Seq[Urn] = affiliations.map(_.user)

  private def contacts(affiliations: Seq[Following]): Seq[Urn] = affiliations.map(_.target)

  private def pageSizeParam(request: HandlerRequest) = {
    request.params
      .get("limit")
      .orElse(request.params.get("page_size"))
      .map(_.toInt)
      .getOrElse(50)
  }

  private def cursorParam(request: HandlerRequest) = request.params.get("cursor")

  private def fetchPage[T](
      request: HandlerRequest,
      fetchFunction: (UserSession, Urn, Option[String], Int) => Future[Option[FollowingsPage]],
      mapUsers: List[User] => List[JsValue],
      users: Seq[Following] => Seq[Urn],
      requireLogin: Boolean
  ): Future[Response] = {
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, userToFetch: Urn) =>
      for {
        affiliationsOption <- fetchFunction(session, userToFetch, cursorParam(request), pageSizeParam(request))
        urns = affiliationsOption.map(page => users(page.followings).toSet).getOrElse(Set.empty)
        users <- fetchUsers(session, urns)
      } yield {
        affiliationsOption
          .map { affiliations =>
            JsonResponseBuilder.ok(
              Json.stringify(
                Json.obj(
                  "collection" -> mapUsers(users),
                  "next_href" -> nextHref(baseUrl, request.request.path, affiliations.next, request.params)
                )
              )
            )
          }
          .getOrElse(ResponseBuilder.serviceUnavailable())
      }
    }
  }

  private def fetchUser(
      request: HandlerRequest,
      filteringFunction: (UserSession, Urn, Seq[Urn]) => Future[Option[FilteredUserUrns]],
      requireLogin: Boolean
  ): Future[Response] =
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, loggedInUser: Urn) =>
      val userId = request.routeParams.get("other_id").get
      val user = Urn("soundcloud", "users", userId)

      for {
        filteredOption <- filteringFunction(session, loggedInUser, Seq(user))
        urns = filteredOption.map(_.included).getOrElse(Set.empty)
        users <- fetchUsers(session, urns)
      } yield {
        filteredOption
          .map { _ =>
            if (users.nonEmpty) {
              JsonResponseBuilder(
                status = Status.SeeOther,
                headers = Map("Location" -> s"$baseUrl/users/$userId"),
                body = Json.stringify(Json.toJson(users.head))
              ).build
            } else {
              ResponseBuilder.notFound()
            }
          }
          .getOrElse(ResponseBuilder.serviceUnavailable())
      }
    }

  private def authenticateIfNeeded(request: HandlerRequest, requireLogin: Boolean)(
      withSession: (UserSession, Urn) => Future[Response]
  ) = {
    if (requireLogin) {
      userAuthentication.withLoggedInUser(request) { (loggedIn, _) =>
        withSession(loggedIn, loggedIn.getUser)
      }
    } else {
      userAuthentication.withUserSession(request) { s =>
        withSession(s, Urn("soundcloud", "users", request.routeParams("id")))
      }
    }
  }

  private def nextHref(
      baseUrl: String,
      path: String,
      next: Option[Pagination],
      requestParams: Map[String, String]
  ): Option[String] = {
    next.map { pagination =>
      val params = requestParams ++ Map("cursor" -> pagination.cursor, "page_size" -> pagination.page_size) -- Seq(
        "limit"
      )
      baseUrl + path + "?" + params.map { case (k, v) => s"$k=$v" }.mkString("&")
    }
  }

  private def fetchUsers(session: UserSession, urns: Set[Urn]): Future[List[User]] = {
    val context = new MappingContext(session)
    for {
      (users, followCountsMap, repostCountsByUrn) <- Future.join(
        okidoki.fetch(session, urns),
        followCountsClient
          .counts(session, urns.toSeq)
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap),
        repostsClient.getRepostCountsByUrnWithFallback(session, urns)
      )
    } yield {
      users.map { user =>
        val userUrn = (user \ "self" \ "urn").as[Urn]
        val followCounts = followCountsMap.get(userUrn)
        new User(user, baseUrl, followCounts, repostCountsByUrn.get(userUrn))(context)
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

  private def denyAgeRestricted(age: Long): Future[Response] = {
    val errors = JsArray(
      Seq(
        JsObject(
          Seq(
            "error_message" -> JsString("DENY_AGE_RESTRICTED"),
            "age" -> JsNumber(age)
          )
        )
      )
    )
    forbidden(errors)
  }

  private def denyAgeUnknown: Future[Response] = {
    val errors = JsArray(
      Seq(
        JsObject(
          Seq(
            "error_message" -> JsString("DENY_AGE_UNKNOWN")
          )
        )
      )
    )
    forbidden(errors)
  }

  private def forbidden(errors: JsArray): Future[Response] = {
    Future.value(
      JsonResponseBuilder(
        status = Status.Forbidden,
        body = Json.stringify(JsObject(Seq("errors" -> errors)))
      ).build
    )
  }
}
