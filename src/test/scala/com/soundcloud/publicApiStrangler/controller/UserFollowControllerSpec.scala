package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.follows.client.FollowsClient
import com.soundcloud.follows.client.representation._
import com.soundcloud.follows.client.representation.follow.{FollowingCreated, UserNotFound, AgeUnknownUser, AgeRestrictedUser}
import com.soundcloud.follows.client.representation.unfollow.{UserAsTarget, UnfollowSuccessful}
import com.soundcloud.jvmkit.{Geo => JvmGeo, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Geo, Urn}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeUtils, LocalDateTime}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._

class UserFollowControllerSpec extends InjectionBasedControllerSpecification with Fixtures {
  sequential

  trait Context extends Scope with BeforeAfter with VerifiedMocks {
    val fallbackMock = mock[DispatchToMothershipHandler]
    val okidokiMock = mock[OkidokiClient]
    val followsMock = mock[FollowsClient]
    val followCountsClientMock = mock[FollowCountsClient]
    val userUrn = Urn("soundcloud:users:999")
    lazy val geo = Geo("US")
    lazy val session = new UserSessionBuilder().setUser(userUrn).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()
    lazy val controller = new UserFollowController(fakeUserAuthentication(session), fallbackMock, okidokiMock, followsMock, followCountsClientMock, "http://foo")
    lazy val userMock = okidokiUsers.as[List[JsObject]].head
    lazy val okidokiResponse = Future(List(userMock))

    val now = System.currentTimeMillis()

    override def before: Any = {
      DateTimeUtils.setCurrentMillisFixed(now)
      okidokiMock.fetch(session, Set(userUrn)) returns okidokiResponse
    }

    override def after = {
      DateTimeUtils.setCurrentMillisSystem()
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
        followCountsClientMock.counts(session, values) returns Future.value(Seq(FollowCounts(values.last, 1111, 2222)))
      }

      val response = get(controller, "/users/999/followers/followed_by/2", Map("limit" -> "10"))
      response.status ==== Status.Ok
      val json = Json.parse(response.body)
      (json \ "collection").as[Seq[JsObject]].size ==== 1
      (json \ "next_href").asOpt[String] ==== None
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
        followCountsClientMock.counts(session, values) returns Future.value(Seq(FollowCounts(values.last, 1111, 2222)))
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
        followCountsClientMock.counts(session, values) returns Future.value(Seq(FollowCounts(values.last, 1111, 2222)))
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
        followCountsClientMock.counts(session, values.map(_.target)) returns Future.value(Seq(FollowCounts(values.map(_.target).last, 1111, 2222)))
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
        followCountsClientMock.counts(session, values.map(_.user)) returns Future.value(Seq(FollowCounts(values.map(_.user).last, 1111, 2222)))
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

      "with follow counts flag on" in new FollowingsContext {

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
        followCountsClientMock.counts(session, values.map(_.user)) returns Future.value(Seq(FollowCounts(values.map(_.user).last, 1111, 2222)))
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
      followCountsClientMock.counts(session, Seq(candidateUser)) returns Future.value(Seq(FollowCounts(candidateUser, 1111, 2222)))
    }
  }

  trait FollowingNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud:users:123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowings(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
      followCountsClientMock.counts(session, Seq.empty) returns Future.value(Seq.empty)
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
      followCountsClientMock.counts(session, Seq(candidateUser)) returns Future.value(Seq(FollowCounts(candidateUser, 1111, 2222)))
    }
  }

  trait FollowerNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud:users:123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowers(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
      followCountsClientMock.counts(session, Seq.empty) returns Future.value(Seq.empty)
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

  "PUT /me/followings/:other_id.json" >> {
    "works like the route without .json" in new Context {
      override def before: Any = {
        super.before
        val following = Following("1", LocalDateTime.now, userUrn, Urn("soundcloud:users:999"))
        followsMock.follow(session, userUrn) returns Future.value(FollowingCreated(following))
        followCountsClientMock.counts(session, Seq(following.target)) returns Future.value(Seq(FollowCounts(following.target, 1111, 2222)))
      }

      val response = put(controller, "/me/followings/999.json", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Created
    }
  }

  "PUT /me/followings/:other_id" >> {
    "follows a profile" in new Context {
      override def before: Any = {
        super.before
        val following = Following("1", LocalDateTime.now, userUrn, Urn("soundcloud:users:999"))
        followsMock.follow(session, userUrn) returns Future.value(FollowingCreated(following))

        followCountsClientMock.counts(session, Seq(following.target)) returns Future.value(Seq(FollowCounts(following.target, 1111, 2222)))
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Created
    }

    "render the age-restricted errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(AgeRestrictedUser)
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(31)
    }

    "render the age-unknown errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(AgeUnknownUser)
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Forbidden
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_UNKNOWN")
    }

    "render regular errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(UserNotFound)
      }

      val response = put(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.NotFound
      val errors = (response.jsonBody \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("404 - Not Found")
    }

  }

  "DELETE /me/followings/:other_id.json" >> {
    "works like the route without .json" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UnfollowSuccessful)
      }

      val response = delete(controller, "/me/followings/999.json", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }
  }

  "DELETE /me/followings/:other_id" >> {
    "unfollows a profile" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UnfollowSuccessful)
      }

      val response = delete(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "render errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UserAsTarget)
      }

      val response = delete(controller, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.UnprocessableEntity
    }
  }
}
