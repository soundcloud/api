package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.{ Json => BffJson }
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.Geo
import com.soundcloud.publicApiStrangler.clients.{Affiliation, FollowsPage, PageInfo, FollowsClient}
import com.soundcloud.publicApiStrangler.features.Rollout
import com.soundcloud.publicApiStrangler.mapping.timeline.User
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.scalakit.{UTF8, Urn, UserSession}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, Years}
import play.api.libs.json.{JsArray, JsNumber, JsValue, Json}

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

  get("/me/followings")(fetchFollowings)
  get("/me/followers")(fetchFollowers)
  get("/me/followers/ids")(fetchFollowerIds)
  get("/me/followings/ids")(fetchFollowingIds)
  get("/users/:id/followers/followed_by/:other_id")(fetchMutualFollowers)
  get("/users/:id/followings/not_followed_by/:other_id")(fetchFollowingsNotFollowedBy)
  get("/users/:id/followings/common_to/:other_id")(fetchMutualFollowings)

  get("/me/followings/:id")(fallbackToMothership)
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
      contacts
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
      contacts
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
      fans
    )
  }

  private def fetchFollowers(request: Request): Future[ResponseBuilder] = fetchFromFollows(request, follows.followers, mapUsersToUsers, fans)

  private def fetchFollowings(request: Request) = fetchFromFollows(request, follows.followings, mapUsersToUsers, contacts)

  private def fetchFollowingIds(request: Request) = fetchFromFollows(request, follows.followings, userIds, contacts)

  private def fetchFollowerIds(request: Request) = fetchFromFollows(request, follows.followers, userIds, fans)

  private def mapUsersToUsers(users: List[User]): List[Any] = users

  private def userIds(users: List[User]): List[Any] = users.map(u => u.id)

  private def fans(affiliations: Seq[Affiliation]): Seq[Urn] = affiliations.map(_.user)

  private def contacts(affiliations: Seq[Affiliation]): Seq[Urn] = affiliations.map(_.target)

  private def pageSizeParam(request: Request) = {
    request.params.get("limit")
      .orElse(request.params.get("page_size"))
      .map(_.toInt).getOrElse(50)
  }

  private def lastIdParam(request: Request) = request.params.get("last_id")

  private def fetchFromFollows(request: Request,
                               fetchFunction: (UserSession, Int, Option[String]) => Future[FollowsPage],
                               mapUsers: List[User] => List[Any] = mapUsersToUsers,
                               users: Seq[Affiliation] => Seq[Urn]): Future[ResponseBuilder] = {
    userAuthentication.withLoggedInUser(request) { (session, userUrn) =>
      if(rollingOutReads(session)) {
        for {
          affiliations <- fetchFunction(session, pageSizeParam(request), lastIdParam(request))
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

  private def rollingOutReads(session: UserSession): Boolean = {
    rollout.isActiveForUser("follows-reads", session.getUser)
  }

  private def nextHref(baseUrl: String, path: String, pageInfo: PageInfo, requestParams: Map[String, String]): Option[String] = {
    pageInfo.lastId.map { nextId =>
      val params = requestParams ++ Map("last_id" -> nextId, "page_size" -> pageInfo.size) -- Seq("limit")
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
      .headers(DefaultResponseHeaders.defaultHeaders)
      .toFuture
  }

  private def denyAgeUnknown: Future[ResponseBuilder] = {
    render.json(Map("errors" -> Seq(Map("error_message" -> "DENY_AGE_UNKNOWN"))))
      .status(Status.Forbidden.getCode)
      .headers(DefaultResponseHeaders.defaultHeaders)
      .toFuture
  }

  private def fallbackToMothership(request: Request): Future[ResponseBuilder] = {
    fallback.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).map { response =>
      val headers = response.headers().entries().map(e => e.getKey -> e.getValue).toMap
      new ResponseBuilder().status(response.getStatusCode()).body(response.getContentString()).headers(headers)
    }
  }
}
