package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Geo => JvmGeo}
import com.soundcloud.publicApiStrangler.clients.{Following, FollowsClient, FollowsPage, PageInfo, _}
import com.soundcloud.publicApiStrangler.features.Rollout
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Geo, Urn, UserSession}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeUtils}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._

class UserFollowControllerSpec extends InjectionBasedControllerSpecification with Fixtures {

  // changes to DateTimeUtils are not thread-safe
  sequential

  trait Context extends Scope with BeforeAfter with VerifiedMocks {
    val fallbackMock = mock[DispatchToMothershipHandler]
    val okidokiMock = mock[OkidokiClient]
    val followsMock = mock[FollowsClient]
    val rollout = mock[Rollout]
    val userUrn = Urn("soundcloud:users:999")
    lazy val geo = Geo("US")
    lazy val session = UserSession(userUrn, Urn("soundcloud:applications:v2"), geo, Set.empty)
    lazy val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, okidokiMock, followsMock, "http://foo", rollout)
    lazy val userMock = okidokiUsers.as[List[JsObject]].head
    lazy val okidokiResponse = Future(List(userMock))

    val now = System.currentTimeMillis()

    override def before = {
      DateTimeUtils.setCurrentMillisFixed(now)
      rollout.isActiveForId("follows-reads", Some(userUrn)) returns true
      rollout.isActiveForId("follows-writes", Some(userUrn)) returns true
      okidokiMock.fetch(session, Set(userUrn)) returns okidokiResponse
    }

    override def after = {
      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  trait FallbackContext extends Context {
    val expectedResponse = Response(Status.Ok)

    override def before = {
      super.before
      rollout.isActiveForId("follows-reads", Some(userUrn)) returns false
      rollout.isActiveForId("follows-writes", Some(userUrn)) returns false

      when(fallbackMock.defaultHandling(any[HandlerRequest])).thenReturn(Future.value(expectedResponse))
    }
  }

  "GET /users/:id/followers/followed_by/:other_id" >> {
    "fetches followings" in new Context {
      override def before = {
        super.before
        val values = Seq(
          Following("123-123", "2012-02-13T23:30:13.000+0000", userUrn, Urn("soundcloud:users:100"))
        )
        val pageInfo = PageInfo(Some("123-1234"), 2)
        followsMock.mutualFollowers(session, userUrn, Urn("soundcloud:users:2"), 10, None) returns Future.value(FollowsPage(values, pageInfo))
        okidokiMock.fetch(session, values.map(_.user).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/users/999/followers/followed_by/2", Map("limit" -> "10"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== Some("http://foo/users/999/followers/followed_by/2?page_size=2&cursor=123-1234")
    }

    "fall back to moshi when not rolling out" in new FallbackContext {
      val response = get(controller, "/users/999/followers/followed_by/2", Map("limit" -> "10"))
      response.status ==== expectedResponse.status
    }
  }

  "GET /users/:id/followings/not_followed_by/:other_id" >> {
    "fetches followings" in new Context {

      override def before = {
        super.before
        val values = Seq(
          Following("123-123", "2012-02-13T23:30:13.000+0000", Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = PageInfo(Some("123-1234"), 2)
        followsMock.followingsNotFollowedBy(session, Urn("soundcloud:users:999"), Urn("soundcloud:users:2"), 10, None) returns Future.value(FollowsPage(values, pageInfo))
        okidokiMock.fetch(session, values.map(_.target).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/users/999/followings/not_followed_by/2", Map("limit" -> "10"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== Some("http://foo/users/999/followings/not_followed_by/2?page_size=2&cursor=123-1234")
    }
  }

  "GET /users/:id/followings/common_to/:other_id" >> {
    "fetches followings" in new Context {

      override def before = {
        super.before
        val values = Seq(
          Following("123-123", "2012-02-13T23:30:13.000+0000", Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = PageInfo(Some("123-1234"), 2)
        followsMock.mutualFollowings(session, Urn("soundcloud:users:999"), Urn("soundcloud:users:2"), 10, Some("2")) returns Future.value(FollowsPage(values, pageInfo))
        okidokiMock.fetch(session, values.map(_.target).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/users/999/followings/common_to/2", Map("limit" -> "10", "cursor" -> "2"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== Some("http://foo/users/999/followings/common_to/2?page_size=2&cursor=123-1234")
    }
  }

  "GET /me/followings/ids" >> {
    "fetches a user's followings" in new Context {

      override def before = {
        super.before
        val values = Seq(
          Following("123-123", "2012-02-13T23:30:13.000+0000", Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = PageInfo(Some("123-1234"), 2)
        followsMock.followings(session, 10, None) returns Future.value(FollowsPage(values, pageInfo))
        okidokiMock.fetch(session, values.map(_.target).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/me/followings/ids", Map("limit" -> "10"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection") ==== JsArray(Seq(JsNumber(123)))
      (json \ "next_href").asOpt[String] ==== Some("http://foo/me/followings/ids?page_size=2&cursor=123-1234")
    }
  }

  "GET /me/followers/ids" >> {
    "fetches a user's followings" in new Context {

      override def before = {
        super.before
        val values = Seq(
          Following("123-123", "2012-02-13T23:30:13.000+0000", Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = PageInfo(Some("123-1234"), 2)
        followsMock.followers(session, 10, None) returns Future.value(FollowsPage(values, pageInfo))
        okidokiMock.fetch(session, values.map(_.user).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/me/followers/ids", Map("limit" -> "10"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection") ==== JsArray(Seq(JsNumber(123)))
      (json \ "next_href").asOpt[String] ==== Some("http://foo/me/followers/ids?page_size=2&cursor=123-1234")
    }
  }

  "GET /me/followings" >> {
    "fetches a user's followings" in new Context {

      override def before = {
        super.before
        val values = Seq(
          Following("123-123", "2012-02-13T23:30:13.000+0000", Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = PageInfo(Some("123-1234"), 2)
        followsMock.followings(session, 10, None) returns Future.value(FollowsPage(values, pageInfo))
        okidokiMock.fetch(session, values.map(_.target).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/me/followings", Map("limit" -> "10", "client_id" -> "FOO"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== Some("http://foo/me/followings?client_id=FOO&page_size=2&cursor=123-1234")
    }
  }

  "GET /me/followers" >> {
    "fetches a user's followers" in new Context {

      override def before = {
        super.before
        val values = Seq(
          Following("123-123", "2012-02-13T23:30:13.000+0000", Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = PageInfo(Some("123-1234"), 2)
        followsMock.followers(session, 10, Some("foo")) returns Future.value(FollowsPage(values, pageInfo))
        okidokiMock.fetch(session, values.map(_.user).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/me/followers", Map("limit" -> "10", "cursor" -> "foo"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== Some("http://foo/me/followers?page_size=2&cursor=123-1234")
    }
  }

  "PUT /me/followings/:id" >> {
    "follows a profile" in new Context {
      override def before = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(FollowSuccessful(userUrn))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "render the age-restricted errors" in new Context {
      override def before = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(FollowFailed(followsAgeRestrictedError))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.PreconditionFailed
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(30)
    }

    "render the age-unknown errors" in new Context {
      override def before = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(FollowFailed(followsAgeUnknownError))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.PreconditionFailed
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_UNKNOWN")
    }

    "render regular errors" in new Context {
      override def before = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(FollowFailed(followsError))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.PreconditionFailed
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("foo")
    }

    "fallback to mothership" >> {
      "should allow user to follow a profile without age restrictions" in new FallbackContext {
        val response = put(controller, "/me/followings/4321", Map("client_id" -> "YOUR_CLIENT_ID"))
        response.status ==== Status.Ok
      }

      "should allow adult US user to follow an age restricted profile" in new FallbackContext {
        override lazy val userMock = Json.obj(
          "date_of_birth" -> new DateTime(now).minusYears(21).toString("yyyy/MM/dd")
        )

        val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))
        response.status ==== Status.Ok
      }

      "should not permit US minor to follow an age restricted profile" in new FallbackContext {
        override lazy val userMock = Json.obj(
          "date_of_birth" -> new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
        )

        val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

        response.status ==== Status.Forbidden
        val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
        (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
        (errors \ "age").asOpt[Long] ==== Option(21)
      }

      "should allow adult DE user to follow an age restricted profile" in new FallbackContext {
        override lazy val geo = Geo("DE")
        override lazy val userMock = Json.obj(
          "date_of_birth" -> new DateTime(now).minusYears(18).toString("yyyy/MM/dd")
        )

        val response = put(controller, "/me/followings/32326572", Map("client_id" -> "YOUR_CLIENT_ID"))

        response.status ==== Status.Ok
      }

      "should not permit DE minor to follow an age restricted profile" in new FallbackContext {
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

      "should not permit user without a date of birth to follow an age restricted profile" in new FallbackContext {
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
}
