package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.services.JsonService
import com.soundcloud.bff.test.ControllerSpecification
import com.soundcloud.jvmkit.{Geo => JvmGeo}
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.soundcloud.scalakit.{Geo, Path, Urn, UserSession}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import org.joda.time.{DateTime, DateTimeUtils}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json.{JsNull, JsObject, Json}

class UserFollowControllerSpec extends ControllerSpecification {

  // changes to DateTimeUtils are not thread-safe
  sequential

  override val controller = new TestBffApp with UserFollowController {
    override lazy val publicApiClient = mock[Service[HttpRequest, HttpResponse]]
    override lazy val moshimoshi = mock[JsonService]
  }

  trait Context extends Scope with BeforeAfter {
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

      controller.publicApiClient.apply(any[HttpRequest]) returns Future.value(Response(Status.Ok))

      controller.userSessionForTest = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("US"), Set.empty)
      put("/me/followings/4321", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status mustEqual Status.Ok
    }

    "should allow adult US user to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val userDob = new DateTime(now).minusYears(21).toString("yyyy/MM/dd")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("US"), Set.empty)

      val moshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      controller.moshimoshi.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshiResp)

      controller.publicApiClient.apply(any[HttpRequest]) returns Future.value(Response(Status.Ok))

      controller.userSessionForTest = session
      put("/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status mustEqual Status.Ok
    }

    "should not permit US minor to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val userDob = new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("US"), Set.empty)

      val moshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      controller.moshimoshi.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshiResp)

      controller.userSessionForTest = session
      put("/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status mustEqual Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] mustEqual Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] mustEqual Option(21)
    }

    "should allow adult DE user to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val userDob = new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("DE"), Set.empty)

      val moshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      controller.moshimoshi.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshiResp)

      controller.publicApiClient.apply(any[HttpRequest]) returns Future.value(Response(Status.Ok))

      controller.userSessionForTest = session
      put("/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status mustEqual Status.Ok
    }

    "should not permit DE minor to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val userDob = new DateTime(now).minusYears(16).toString("yyyy/MM/dd")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), Geo("DE"), Set.empty)

      val moshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> userDob))
      controller.moshimoshi.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshiResp)

      controller.userSessionForTest = session
      put("/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status mustEqual Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] mustEqual Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] mustEqual Option(18)
    }

    "should not permit user without a date of birth to follow an age restricted profile" in new Context {
      val userUrn = Urn("soundcloud:users:999")
      val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), JvmGeo.UNKNOWN_GEO, Set.empty)

      val moshiResp = JsonResponse(OkStatus, Json.obj("date_of_birth" -> JsNull))
      controller.moshimoshi.get(===(session), ===(Path() / "users" / userUrn), any[Params], any[Params]) returns Future.value(moshiResp)

      controller.userSessionForTest = session
      put("/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status mustEqual Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] mustEqual Option("DENY_AGE_UNKNOWN")
    }
  }
}
