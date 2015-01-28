package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.Geo
import com.soundcloud.publicApiStrangler.support.{DispatchToMothershipHandler, PublicApiClientComponent}
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.Params
import com.soundcloud.scalakit.{Path, UTF8, Urn, UserSession}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, Years}
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.io.Source

trait UserFollowController extends BffController with PublicApiClientComponent {

  val followRestrictions = {
    val source = Source.fromInputStream(getClass.getResourceAsStream("/user-follow-restrictions.json"), UTF8.name())
    try {
      Json.parse(source.mkString)
    } finally {
      source.close()
    }
  }

  val fallback = new DispatchToMothershipHandler(publicApiClient)

  val formatter = DateTimeFormat.forPattern("yyyy/M/d")

  get("/me/followings/:id")(fallbackToMothership)
  head("/me/followings/:id")(fallbackToMothership)
  post("/me/followings/:id")(fallbackToMothership)
  patch("/me/followings/:id")(fallbackToMothership)
  delete("/me/followings/:id")(fallbackToMothership)

  put("/me/followings/:id") { request =>
    withLoggedInUser(request) { (session, userUrn) =>
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

  private def findAgeRestriction(userId: String, geo: Geo): Option[Long] = {
    val restrictions = followRestrictions \ userId \ "age"
    (restrictions \ geo.getCountryCode).asOpt[Long].orElse((restrictions \ "*").asOpt[Long])
  }

  private def findUserAge(session: UserSession, userUrn: Urn): Future[Option[Long]] = {
    moshimoshi.get(session, Path() / "users" / userUrn, Params.empty).map { response =>
      response.status match {
        case OkStatus => (response.body \ "date_of_birth").asOpt[String].map(currentAge)
        case _ => None
      }
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
