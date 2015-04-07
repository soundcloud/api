package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.services.JsonService
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Geo => JvmGeo}
import com.soundcloud.publicApiStrangler.clients.FollowsClient
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{HandlerRequest, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.soundcloud.scalakit.{Geo, Path, Urn, UserSession}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeUtils}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json.{JsString, JsNull, JsObject, Json}

class UserFollowControllerSpec extends InjectionBasedControllerSpecification {
  trait Context extends Scope with BeforeAfter {
    val fallbackMock = mock[DispatchToMothershipHandler]
    val okidokiMock = mock[OkidokiClient]
    val followsMock = mock[FollowsClient]
    val userUrn = Urn("soundcloud:users:999")
    lazy val geo = Geo("US")
    val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), geo, Set.empty)
    lazy val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, okidokiMock, followsMock, "foo")
    lazy val userMock = mock[JsObject]
    lazy val okidokiResponse = Future(List(userMock))

    val now = System.currentTimeMillis()

    override def before = {
      DateTimeUtils.setCurrentMillisFixed(now)
      okidokiMock.fetch(session, Set(userUrn)) returns okidokiResponse
    }

    override def after = {
      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  "GET /me/followings" >> {
    "fetches a user's followings" in new Context {
      val response = get(controller, "/me/followings", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }
  }

  "PUT /me/followings/:id" >> {
    "should allow user to follow a profile without age restrictions" in new Context {
      fallbackMock.defaultHandling(any[HandlerRequest]) returns Future.value(Response(Status.Ok))

      val response = put(controller, "/me/followings/4321", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "should allow adult US user to follow an age restricted profile" in new Context {
      override lazy val userMock = Json.obj(
        "date_of_birth" -> new DateTime(now).minusYears(21).toString("yyyy/MM/dd")
      )

      fallbackMock.defaultHandling(any[HandlerRequest]) returns Future.value(Response(Status.Ok))

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "should not permit US minor to follow an age restricted profile" in new Context {
      override lazy val userMock = Json.obj(
        "date_of_birth" -> new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
      )

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(21)
    }

    "should allow adult DE user to follow an age restricted profile" in new Context {
      override lazy val geo = Geo("DE")
      override lazy val userMock = Json.obj(
        "date_of_birth" -> new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
      )

      fallbackMock.defaultHandling(any[HandlerRequest]) returns Future.value(Response(Status.Ok))

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Ok
    }

    "should not permit DE minor to follow an age restricted profile" in new Context {
      override lazy val geo = Geo("DE")
      override lazy val userMock = Json.obj(
        "date_of_birth" -> new DateTime(now).minusYears(16).toString("yyyy/MM/dd")
      )

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(18)
    }

    "should not permit user without a date of birth to follow an age restricted profile" in new Context {
      override lazy val geo = JvmGeo.UNKNOWN_GEO
      override lazy val userMock = Json.obj(
        "date_of_birth" -> JsNull
      )

      val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_UNKNOWN")
    }
  }
}
