package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{LoggedInUserSession, UserSession}
import com.soundcloud.apipublic.client.chrono.ChronoResponse
import com.soundcloud.apipublic.client.follows._
import com.soundcloud.apipublic.client.follows.representation._
import com.soundcloud.apipublic.client.follows.representation.follow._
import com.soundcloud.apipublic.client.follows.representation.unfollow.{
  NotFollowing,
  UnfollowSuccessful,
  UnknownError => UnfollowUnknownError,
  UserAsTarget => UnfollowUserAsTarget,
  UserNotFound => UnfollowUserNotFound
}
import com.soundcloud.apipublic.client.mothership.OkidokiClient
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.support.{ErrorResponse, UserUrnUtil}
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, LocalDate, Years}
import play.api.libs.json._

object UserFollowHandler {
  val MaxFollowingsReachedMessage =
    "You have reached the maximum number of users you can follow. To follow a new user, unfollow another user first."

  // Shown for follow rejections whose error name api-public does not recognize.
  val FollowingRejectedMessage = "This user cannot be followed."
}

class UserFollowHandler(
    userAuthentication: UserAuthentication,
    okidoki: OkidokiClient,
    follows: FollowsClient,
    userRepresentationsService: UserRepresentationsService,
    baseUrl: String
) {
  val formatter = DateTimeFormat.forPattern("yyyy/M/d")

  def follow(request: HandlerRequest): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      Try(UserUrnUtil.getUserUrn(request.routeParams("other_id"))) match {
        case Return(user) =>
          follows.follow(session, user).flatMap {
            case _: FollowingCreated => renderFollow(session, user)
            case AlreadyFollowing => renderStatus()
            case UserNotFound => renderError(Status.NotFound)
            case SpamBlocked => renderError(Status.TooManyRequests)
            case MaxFollowingsReached =>
              renderError(Status.UnprocessableEntity, UserFollowHandler.MaxFollowingsReachedMessage)
            case FollowingRejected(_) =>
              renderError(Status.UnprocessableEntity, UserFollowHandler.FollowingRejectedMessage)
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
        case Throw(_) => renderError(Status.BadRequest)
      }
    }

  def unfollow(request: HandlerRequest): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(UserUrnUtil.getUserUrn(request.routeParams("other_id"))) match {
        case Return(user) =>
          follows.unfollow(session, user).flatMap {
            case UnfollowSuccessful => renderStatus()
            case UnfollowUserNotFound => renderError(Status.NotFound)
            case UnfollowUserAsTarget | NotFollowing => renderError(Status.UnprocessableEntity)
            case _: UnfollowUnknownError => renderError(Status.InternalServerError)
          }
        case Throw(_) => renderError(Status.BadRequest)
      }
    }

  private def renderFollow(session: LoggedInUserSession, target: Urn): Future[Response] = {
    fetchUsers(session, Set(target)).map { users =>
      JsonResponseBuilder.created(
        users.headOption
          .map(user => Json.stringify(Json.toJson(user)))
          .getOrElse(Json.stringify(JsNull))
      )
    }
  }

  private def renderStatus(): Future[Response] =
    Future.value(
      JsonResponseBuilder(
        Status.Ok,
        body = Json.stringify(Json.obj("status" -> "200 - Successful"))
      ).build
    )

  private def renderError(status: Status, message: String = ""): Future[Response] =
    Future.value(ErrorResponse(status, message))

  def fetchFollowersWithoutAuth(request: HandlerRequest): Future[Response] =
    fetchPage(request, follows.followers, mapUsersToUsers, fans, requireLogin = false)

  def fetchFollowingsWithoutAuth(request: HandlerRequest): Future[Response] =
    fetchPage(request, follows.followings, mapUsersToUsers, contacts, requireLogin = false)

  def fetchMyFollowers(request: HandlerRequest): Future[Response] =
    fetchPage(request, follows.followers, mapUsersToUsers, fans, requireLogin = true)

  def fetchMyFollowingsChrono(request: HandlerRequest): Future[Response] =
    fetchPageChrono(request, follows.followingsChrono, mapUsersToUsers, requireLogin = true)

  def fetchFollowings(request: HandlerRequest) =
    fetchPage(request, follows.followings, mapUsersToUsers, contacts, requireLogin = true)

  def fetchFollowingIdsWithoutAuth(request: HandlerRequest) =
    fetchPage(request, follows.followings, mapUsersToUrns, contacts, requireLogin = false)

  def fetchPossibleFollowingWithoutAuth(request: HandlerRequest) =
    fetchUser(request, follows.filterFollowings, requireLogin = false)

  def fetchPossibleFollowerWithoutAuth(request: HandlerRequest) =
    fetchUser(request, follows.filterFollowers, requireLogin = false)

  def fetchPossibleFollowing(request: HandlerRequest) =
    fetchUser(request, follows.filterFollowings, requireLogin = true)

  def fetchPossibleFollower(request: HandlerRequest) = fetchUser(request, follows.filterFollowers, requireLogin = true)

  private def mapUsersToUsers(users: List[UserRepresentation], nextHref: Option[String]): String = {
    val userCollection = Collection[UserRepresentation](users, nextHref)
    Collection.getRepresentation(userCollection, true)
  }

  private def mapUsersToUrns(users: List[UserRepresentation], nextHref: Option[String]): String = {
    val urns = users.map(u => u.urn)
    val urnCollection = Collection(urns, nextHref)
    Collection.getRepresentation(urnCollection, true)
  }

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

  private def limitChronoParam(request: HandlerRequest): Int =
    request.params
      .get("limit")
      .orElse(request.params.get("page_size"))
      .flatMap(s => scala.util.Try(s.toInt).toOption)
      .getOrElse(20)

  private def directionChronoParam(request: HandlerRequest): String =
    request.params
      .get("direction")
      .orElse(request.params.get("order"))
      .filter(d => d == "asc" || d == "desc")
      .getOrElse("asc")

  private def fetchPageChrono(
      request: HandlerRequest,
      fetchFunction: (UserSession, Urn, Option[String], Int, String) => Future[Option[ChronoResponse]],
      serializeUsers: (List[UserRepresentation], Option[String]) => String,
      requireLogin: Boolean
  ): Future[Response] =
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, userToFetch: Urn) =>
      fetchFunction(
        session,
        userToFetch,
        cursorParam(request),
        limitChronoParam(request),
        directionChronoParam(request)
      ).flatMap {
        case None => Future.value(ErrorResponse(Status.ServiceUnavailable))
        case Some(chrono) =>
          val urns = chrono.items.map(_.urn).toSet
          fetchUsers(session, urns).map { users =>
            val next = nextHrefChrono(
              baseUrl,
              request.request.path,
              request.params,
              chrono.items.lastOption.map(_.cursor),
              limitChronoParam(request),
              directionChronoParam(request)
            )
            JsonResponseBuilder.ok(serializeUsers(users, next))
          }
      }
    }

  private def fetchPage[T](
      request: HandlerRequest,
      fetchFunction: (UserSession, Urn, Option[String], Int) => Future[Option[FollowingsPage]],
      serializeUsers: (List[UserRepresentation], Option[String]) => String,
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
            val next = nextHref(baseUrl, request.request.path, affiliations.next, request.params)
            JsonResponseBuilder.ok(serializeUsers(users, next))
          }
          .getOrElse(ErrorResponse(Status.ServiceUnavailable))
      }
    }
  }

  private def fetchUser(
      request: HandlerRequest,
      filteringFunction: (UserSession, Urn, Seq[Urn]) => Future[Option[FilteredUserUrns]],
      requireLogin: Boolean
  ): Future[Response] =
    authenticateIfNeeded(request, requireLogin) { (session, loggedInUser) =>
      Try(UserUrnUtil.getUserUrn(request.routeParams("other_id"))) match {
        case Return(user) =>
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
                    headers = Map("Location" -> s"$baseUrl/users/${user.identifier}"),
                    body = Json.stringify(Json.toJson(users.head))
                  ).build
                } else {
                  ResponseBuilder.notFound()
                }
              }
              .getOrElse(ErrorResponse(Status.ServiceUnavailable))
          }
        case Throw(_) => renderError(Status.BadRequest)
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
        withSession(s, UserUrnUtil.getUserUrn(request.routeParams("id")))
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
        "limit",
        "client_id"
      )
      baseUrl + path + "?" + params.map { case (k, v) => s"$k=$v" }.mkString("&")
    }
  }

  private def nextHrefChrono(
      baseUrl: String,
      path: String,
      requestParams: Map[String, String],
      cursor: Option[String],
      limit: Int,
      direction: String
  ): Option[String] =
    cursor.map { c =>
      val params = requestParams ++ Map("cursor" -> c, "limit" -> limit.toString, "direction" -> direction) - "client_id"
      baseUrl + path + "?" + params.map { case (k, v) => s"$k=$v" }.mkString("&")
    }

  private def fetchUsers(session: UserSession, urns: Set[Urn]): Future[List[UserRepresentation]] = {
    userRepresentationsService.users(session, urns.toSeq, fetchSubscriptions = false)
  }

  private def findUserAge(session: UserSession, userUrn: Urn): Future[Option[Int]] = {
    okidoki.fetch(session, Set(userUrn)).map {
      case user :: _ => (user \ "date_of_birth").asOpt[String].map(currentAge)
      case _ => None
    }
  }

  private def currentAge(dateOfBirth: String): Int = {
    val dob = formatter.parseLocalDate(dateOfBirth)
    Years.yearsBetween(dob, new LocalDate(DateTimeZone.UTC)).getYears
  }

  private def denyAgeRestricted(age: Long): Future[Response] = {
    val errors = Map("error_message" -> JsString("DENY_AGE_RESTRICTED"), "age" -> JsNumber(age))
    Future.value(ErrorResponse(Status.Forbidden, "DENY_AGE_RESTRICTED", None, Some(errors)))
  }

  private def denyAgeUnknown: Future[Response] = Future.value(ErrorResponse(Status.Forbidden, "DENY_AGE_UNKNOWN"))
}
