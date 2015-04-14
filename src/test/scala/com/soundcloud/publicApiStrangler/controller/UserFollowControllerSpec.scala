package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.services.JsonService
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Geo => JvmGeo}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.soundcloud.scalakit.{Geo, Path, Urn, UserSession}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeUtils}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json.{JsNull, JsObject, Json}

class UserFollowControllerSpec extends InjectionBasedControllerSpecification {
  trait Context extends Scope with BeforeAfter {
    val fallbackMock = mock[DispatchToMothershipHandler]
    val moshimoshiMock = mock[JsonService]

    val now = System.currentTimeMillis()

    override def before = {
      DateTimeUtils.setCurrentMillisFixed(now)
    }

    override def after = {
      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  "PUT /me/followings/:id" >> {
    "should allow user to follow a profile without age restrictions" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("US"), Set.empty)
      val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, moshimoshiMock)

      fallbackMock.defaultHandling(any[HandlerRequest]) returns Future.value(Response(Status.Ok))

      val response = put(controller, "/me/followings/4321", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "should allow adult US user to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("US"), Set.empty)
      val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, moshimoshiMock)

      val userDob = new DateTime(now).minusYears(21).toString("yyyy/MM/dd")
      val moshimoshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      moshimoshiMock.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshimoshiResp)

      fallbackMock.defaultHandling(any[HandlerRequest]) returns Future.value(Response(Status.Ok))

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "should not permit US minor to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val userDob = new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("US"), Set.empty)

      val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, moshimoshiMock)

      val moshimoshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      moshimoshiMock.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshimoshiResp)

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(21)
    }

    "should allow adult DE user to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("DE"), Set.empty)
      val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, moshimoshiMock)

      val userDob = new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
      val moshimoshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      moshimoshiMock.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshimoshiResp)

      fallbackMock.defaultHandling(any[HandlerRequest]) returns Future.value(Response(Status.Ok))

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Ok
    }

    "should not permit DE minor to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("DE"), Set.empty)
      val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, moshimoshiMock)

      val userDob = new DateTime(now).minusYears(16).toString("yyyy/MM/dd")
      val moshimoshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      moshimoshiMock.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshimoshiResp)

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(18)
    }

    "should not permit user without a date of birth to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), JvmGeo.UNKNOWN_GEO, Set.empty)
      val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, moshimoshiMock)

      val moshimoshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> JsNull))
      moshimoshiMock.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshimoshiResp)

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_UNKNOWN")
    }
  }
}
