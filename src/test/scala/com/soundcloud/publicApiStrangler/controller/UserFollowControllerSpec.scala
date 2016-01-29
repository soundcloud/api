package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.follows.client.FollowsClient
import com.soundcloud.follows.client.representation._
import com.soundcloud.jvmkit.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.{Geo => JvmGeo, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Geo, Urn}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import org.joda.time.{LocalDateTime, DateTime, DateTimeUtils}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._

class UserFollowControllerSpec extends InjectionBasedControllerSpecification with Fixtures {
  sequential

  trait Context extends Scope with BeforeAfter with VerifiedMocks {
    val fallbackMock = mock[DispatchToMothershipHandler]
    val okidokiMock = mock[OkidokiClient]
    val followsMock = mock[FollowsClient]
    val followCountsClientMock = mock[FollowCountsClient]
    val rollout = mock[Rollout]
    val userUrn = Urn("soundcloud:users:999")
    lazy val geo = Geo("US")
    lazy val session = new UserSessionBuilder().setUser(userUrn).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()
    lazy val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, okidokiMock, followsMock, followCountsClientMock, "http://foo", rollout)
    lazy val userMock = okidokiUsers.as[List[JsObject]].head
    lazy val okidokiResponse = Future(List(userMock))

    val now = System.currentTimeMillis()

    def followCountsFlag: Boolean = false

    override def before: Any = {
      DateTimeUtils.setCurrentMillisFixed(now)
      rollout.isActiveForUrn(BasicRolloutFeature("follows-reads"), userUrn) returns Future.True
      rollout.isActiveForUrn(BasicRolloutFeature("follows-writes"), userUrn) returns Future.True
      rollout.isActive(BasicRolloutFeature("follow-counts-from-stitch")) returns Future.value(followCountsFlag)
      okidokiMock.fetch(session, Set(userUrn)) returns okidokiResponse
    }

    override def after = {
      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  trait FallbackContext extends Context {
    val expectedResponse = Response(Status.Ok)

    override def before: Any = {
      super.before
      rollout.isActiveForUrn(BasicRolloutFeature("follows-reads"), userUrn) returns Future.False
      rollout.isActiveForUrn(BasicRolloutFeature("follows-writes"), userUrn) returns Future.False

      when(fallbackMock.defaultHandling(any[HandlerRequest])).thenReturn(Future.value(expectedResponse))
    }
  }

  "GET /users/:id/followers/followed_by/:other_id" >> {
    "fetches followings" in new Context {
      override def before: Any = {
        super.before
        val values = Seq(userUrn, Urn("soundcloud:users:100"))
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followersFollowedBy(session, userUrn, Urn("soundcloud:users:2")) returns Future.value(Some(UserUrns(values)))
        okidokiMock.fetch(session, values.toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/users/999/followers/followed_by/2", Map("limit" -> "10"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== None
    }

    "fall back to moshi when not rolling out" in new FallbackContext {
      val response = get(controller, "/users/999/followers/followed_by/2", Map("limit" -> "10"))
      response.status ==== expectedResponse.status
    }
  }

  "GET /users/:id/followings/not_followed_by/:other_id" >> {
    "fetches followings" in new Context {

      override def before: Any = {
        super.before
        val values = Seq(
          Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100")
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followingsNotFollowedBy(session, Urn("soundcloud:users:999"), Urn("soundcloud:users:2")) returns Future.value(Some(UserUrns(values)))
        okidokiMock.fetch(session, values.toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/users/999/followings/not_followed_by/2", Map("limit" -> "10"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== None
    }
  }

  "GET /users/:id/followings/common_to/:other_id" >> {
    "fetches followings" in new Context {

      override def before: Any = {
        super.before
        val values = Seq(
          Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100")
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.mutualFollowings(session, Urn("soundcloud:users:999"), Urn("soundcloud:users:2")) returns Future.value(Some(UserUrns(values)))
        okidokiMock.fetch(session, values.toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/users/999/followings/common_to/2", Map("limit" -> "10", "cursor" -> "2"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== None
    }
  }

  "GET /me/followings/ids" >> {
    "fetches a user's followings" in new Context {

      override def before: Any = {
        super.before
        val values = Seq(
          Following("123-123", new LocalDateTime("2012-02-13T23:30:13.000"), Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followings(session, session.getUser, None, 10) returns Future.value(Some(FollowingsPage(values, Some(pageInfo))))
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

      override def before: Any = {
        super.before
        val values = Seq(
          Following("123-123", new LocalDateTime("2012-02-13T23:30:13.000"), Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followers(session, session.getUser, None, 10) returns Future.value(Some(FollowingsPage(values, Some(pageInfo))))
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
    trait FollowingsContext extends Context {

      val followings = Seq(
        Following("123-123", new LocalDateTime("2012-02-13T23:30:13.000"), Urn("soundcloud:users:123"), userUrn)
      )

      override def before: Any = {
        super.before
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followings(session, session.getUser, None, 10) returns Future.value(Some(FollowingsPage(followings, Some(pageInfo))))
        okidokiMock.fetch(session, followings.map(_.target).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }
    }

    "fetches a user's followings" >> {

      "with follow counts flag off" in new FollowingsContext {
        val response = get(controller, "/me/followings", Map("limit" -> "10", "client_id" -> "FOO"))
        response.status ==== Status.Ok
        val json = Json.parse(response.body)
        (json \ "collection").as[Seq[JsObject]].size ==== 1
        (json \ "next_href").asOpt[String] ==== Some("http://foo/me/followings?client_id=FOO&page_size=2&cursor=123-1234")
        (json \ "collection" \\ "followers_count").head.as[Long] ==== 20976
        (json \ "collection" \\ "followings_count").head.as[Long] ==== 118
      }

      "with follow counts flag on" in new FollowingsContext {

        override def followCountsFlag: Boolean = true

        override def before: Any = {
          super.before
          followCountsClientMock.counts(session, followings.map(_.target)) returns Future.value(Seq(FollowCounts(followings.head.target, 1111, 2222)))
        }

        val response = get(controller, "/me/followings", Map("limit" -> "10", "client_id" -> "FOO"))
        response.status ==== Status.Ok
        val json = Json.parse(response.body)
        (json \ "collection").as[Seq[JsObject]].size ==== 1
        (json \ "next_href").asOpt[String] ==== Some("http://foo/me/followings?client_id=FOO&page_size=2&cursor=123-1234")
        (json \ "collection" \\ "followers_count").head.as[Long] ==== 1111
        (json \ "collection" \\ "followings_count").head.as[Long] ==== 2222
      }
    }
  }

  "GET /me/followers" >> {
    "fetches a user's followers" in new Context {

      override def before: Any = {
        super.before
        val values = Seq(
          Following("123-123", new LocalDateTime("2012-02-13T23:30:13.000"), Urn("soundcloud:users:12490957"), Urn("soundcloud:users:100"))
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followers(session, session.getUser, Some("foo"), 10) returns Future.value(Some(FollowingsPage(values, Some(pageInfo))))
        okidokiMock.fetch(session, values.map(_.user).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }

      val response = get(controller, "/me/followers", Map("limit" -> "10", "cursor" -> "foo"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== Some("http://foo/me/followers?page_size=2&cursor=123-1234")
    }
  }

  trait FetchesFollowingContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud:users:123")
      val filteredUserUrns = FilteredUserUrns(included = Set(candidateUser), excluded = Set.empty)

      followsMock.filterFollowings(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set(candidateUser)) returns Future.value(okidokiUsers.as[List[JsObject]])
    }
  }

  trait FollowingNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud:users:123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowings(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
    }
  }

  "GET /me/followings/:other_id" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(controller, "/me/followings/123")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(controller, "/me/followings/123")
      response.status ==== Status.NotFound
    }
  }

  "GET /me/followings/:other_id.json" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(controller, "/me/followings/123.json")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(controller, "/me/followings/123.json")
      response.status ==== Status.NotFound
    }
  }

  "GET /users/:id/followings/:other_id" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(controller, "/users/999/followings/123")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(controller, "/users/999/followings/123")
      response.status ==== Status.NotFound
    }
  }

  "GET /users/:id/followings/:other_id.json" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(controller, "/users/999/followings/123.json")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(controller, "/users/999/followings/123.json")
      response.status ==== Status.NotFound
    }
  }

  trait FetchesFollowerContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud:users:123")
      val filteredUserUrns = FilteredUserUrns(included = Set(candidateUser), excluded = Set.empty)

      followsMock.filterFollowers(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set(candidateUser)) returns Future.value(okidokiUsers.as[List[JsObject]])
    }
  }

  trait FollowerNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud:users:123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowers(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
    }
  }

  "GET /me/followers/:other_id" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(controller, "/me/followers/123")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(controller, "/me/followers/123")
      response.status ==== Status.NotFound
    }
  }

  "GET /me/followers/:other_id.json" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(controller, "/me/followers/123.json")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(controller, "/me/followers/123.json")
      response.status ==== Status.NotFound
    }
  }

  "GET /users/:id/followers/:other_id" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(controller, "/users/999/followers/123")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(controller, "/users/999/followers/123")
      response.status ==== Status.NotFound
    }
  }

  "GET /users/:id/followers/:other_id.json" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(controller, "/users/999/followers/123.json")
      response.status ==== Status.SeeOther
      response.getHeader("Location") ==== "http://foo/users/123"

      val json = Json.parse(response.body)
      (json \ "id").as[Long] ==== 123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(controller, "/users/999/followers/123.json")
      response.status ==== Status.NotFound
    }
  }


  "PUT /me/followings/:other_id" >> {
    "follows a profile" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(FollowSuccessful(userUrn))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "render the age-restricted errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(FollowFailed(followsAgeRestrictedError))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.PreconditionFailed
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(31)
    }

    "render the age-unknown errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(FollowFailed(followsAgeUnknownError))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.PreconditionFailed
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_UNKNOWN")
    }

    "render regular errors" in new Context {
      override def before: Any = {
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

  "DELETE /me/followings/:other_id" >> {
    "unfollows a profile" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UnfollowSuccessful(userUrn))
      }

      val response = delete(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "render errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UnfollowFailed(followsError))
      }

      val response = delete(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.PreconditionFailed
    }
  }
}
