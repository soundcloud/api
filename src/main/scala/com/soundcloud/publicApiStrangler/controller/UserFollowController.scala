package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.follows._
import com.soundcloud.publicApiStrangler.features.Rollout
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
  get("/users/:id/followers/followed_by/:other_id")(fetchFollowersFollowed)
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
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      if(rollingOutWrites(userUrn)) {
        val user = Urn("soundcloud:users:" + request.routeParams.get("id").get)
        follows.follow(session, user).flatMap {
          case success: FollowSuccessful => renderFollow(session, success)
          case error: FollowFailed => renderFollowFailed(error, session, user)
        }
      } else {
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
      follows.followersFollowed(
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

  private def fetchMyFollowingIds(request: Request) = fetchPage(request, follows.followings, userIds, contacts, requireLogin = true)

  private def fetchMyFollowerIds(request: Request) = fetchPage(request, follows.followers, userIds, fans, requireLogin = true)

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
                               fetchFunction: (UserSession, Urn) => Future[UrnsPage]): Future[ResponseBuilder] = {
    authenticateIfNeeded(request, requireLogin = false) { (session: UserSession, userToFetch: Urn) =>
      if(rollingOutReads(userToFetch)) {
        for {
          response <- fetchFunction(session, userToFetch)
          urns = response.values
          users <- fetchUsers(session, urns.toSet)
        } yield {
          render.json(Map(
            "collection" -> mapUsersToUsers(users)
          ))
        }
      } else {
        fallbackToMothership(request)
      }
    }
  }

  private def fetchPage(request: Request,
                         fetchFunction: (UserSession, Urn, Int, Option[String]) => Future[FollowsPage],
                         mapUsers: List[User] => List[Any] = mapUsersToUsers,
                         users: Seq[Following] => Seq[Urn],
                         requireLogin: Boolean): Future[ResponseBuilder] = {
    authenticateIfNeeded(request, requireLogin) { (session: UserSession, userToFetch: Urn) =>
      if(rollingOutReads(userToFetch)) {
        for {
          affiliations <- fetchFunction(session, userToFetch, pageSizeParam(request), cursorParam(request))
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

  private def authenticateIfNeeded(request: Request, requireLogin: Boolean)(withSession: (UserSession, Urn) => Future[ResponseBuilder]) = {
    if(requireLogin) {
      userAuthentication.withLoggedInUser(request) { (loggedIn, _) => withSession(loggedIn, loggedIn.getUser) }
    } else {
      userAuthentication.withUserSession(request){ s => withSession(s, Urn(s"soundcloud:users:${request.routeParams("id")}")) }
    }
  }

  private def rollingOutReads(userToFetch: Urn): Boolean = rollout.isActiveForId("follows-reads", Option(userToFetch))

  private def rollingOutWrites(userUrn: Urn): Boolean = rollout.isActiveForId("follows-writes", Option(userUrn))

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
