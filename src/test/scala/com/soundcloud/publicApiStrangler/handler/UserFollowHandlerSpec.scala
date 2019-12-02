package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.follows.FollowsClient
import com.soundcloud.publicApiStrangler.client.follows.representation._
import com.soundcloud.publicApiStrangler.client.follows.representation.follow.{
  AgeRestrictedUser,
  AgeUnknownUser,
  FollowingCreated,
  UserNotFound
}
import com.soundcloud.publicApiStrangler.client.follows.representation.unfollow.{UnfollowSuccessful, UserAsTarget}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.{DateTimeUtils, DateTime}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._

class UserFollowHandlerSpec extends UnitSpecification {
  sequential

  trait Context extends HandlerSpecificationScope with BeforeAfter {
    val okidokiMock = mock[OkidokiClient]
    val followsMock = mock[FollowsClient]
    val followCountsClientMock = mock[FollowCountsClient]
    val repostsClientMock = mock[RepostsClient]
    val userUrn = Urn("soundcloud", "users", "999")
    lazy val geo = new Geo("US")
    lazy val session =
      new UserSessionBuilder().setUser(userUrn).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()
    lazy val handler = new UserFollowHandler(
      new FakeUserAuthentication(session),
      okidokiMock,
      followsMock,
      followCountsClientMock,
      repostsClientMock,
      "http://foo"
    )

    override def routingDefinitions = Routing.forUserFollowHandler(handler)

    lazy val userMock = okidokiUsers.as[List[JsObject]].head
    lazy val okidokiResponse = Future(List(userMock))

    val now = System.currentTimeMillis()

    val user123 = Json.parse("""
        |{
        |  "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
        |  "id": 123,
        |  "kind": "user",
        |  "permalink_url": "http://soundcloud.com/adeline",
        |  "uri": "http://foo/users/123",
        |  "username": "adeline",
        |  "permalink": "adeline",
        |  "last_modified": "2014/10/04 10:48:34 +0000",
        |  "first_name": "Adeline",
        |  "last_name": null,
        |  "full_name": "Adeline",
        |  "city": "London",
        |  "description": "For Adeline bookings worldwide",
        |  "country": null,
        |  "track_count": 49,
        |  "public_favorites_count": 5,
        |  "followers_count": 1111,
        |  "followings_count": 2222,
        |  "plan": "Pro Plus",
        |  "myspace_name": null,
        |  "discogs_name": null,
        |  "website_title": "Adeline Website",
        |  "website": "http://www.adelinemusic.com",
        |  "reposts_count": null,
        |  "comments_count": null,
        |  "online": false,
        |  "likes_count": 5,
        |  "playlist_count": null
        |}
      """.stripMargin)

    val anotherUser123 = Json.parse(
      """
        |{
        |  "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
        |  "id": 123,
        |  "kind": "user",
        |  "permalink_url": "http://soundcloud.com/adeline",
        |  "uri": "http://foo/users/123",
        |  "username": "adeline",
        |  "permalink": "adeline",
        |  "last_modified": "2014/10/04 10:48:34 +0000",
        |  "first_name": "Adeline",
        |  "last_name": null,
        |  "full_name": "Adeline",
        |  "city": "London",
        |  "description": "For Adeline bookings worldwide",
        |  "country": null,
        |  "track_count": 49,
        |  "public_favorites_count": 5,
        |  "followers_count": 20976,
        |  "followings_count": 118,
        |  "plan": "Pro Plus",
        |  "myspace_name": null,
        |  "discogs_name": null,
        |  "website_title": "Adeline Website",
        |  "website": "http://www.adelinemusic.com",
        |  "reposts_count": null,
        |  "comments_count": null,
        |  "online": false,
        |  "likes_count": 5,
        |  "playlist_count": null
        |}
      """.stripMargin
    )

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
        val values = Seq(userUrn, Urn("soundcloud", "users", "100"))
        followsMock.followersFollowedBy(session, userUrn, Urn("soundcloud", "users", "2")) returns Future.value(
          Some(UserUrns(values))
        )
        okidokiMock.fetch(session, values.toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
        followCountsClientMock.counts(session, values) returns Future.value(Seq(FollowCounts(values.last, 1111, 2222)))
        repostsClientMock.getRepostCountsByUrnWithFallback(session, values.toSet) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response = get(handler.fetchFollowersFollowed, "/users/999/followers/followed_by/2", Map("limit" -> "10"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj("collection" -> List(anotherUser123))
    }
  }

  "GET /users/:id/followings/not_followed_by/:other_id" >> {
    "fetches followings" in new Context {
      override def before: Any = {
        super.before
        val values = Seq(
          Urn("soundcloud", "users", "12490957"),
          Urn("soundcloud", "users", "100")
        )
        followsMock.followingsNotFollowedBy(session, Urn("soundcloud", "users", "999"), Urn("soundcloud", "users", "2")) returns Future
          .value(Some(UserUrns(values)))
        okidokiMock.fetch(session, values.toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
        followCountsClientMock.counts(session, values) returns Future.value(Seq(FollowCounts(values.last, 1111, 2222)))
        repostsClientMock.getRepostCountsByUrnWithFallback(session, values.toSet) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response =
        get(handler.fetchFollowingsNotFollowedBy, "/users/999/followings/not_followed_by/2", Map("limit" -> "10"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj("collection" -> List(anotherUser123))
    }
  }

  "GET /users/:id/followings/common_to/:other_id" >> {
    "fetches followings" in new Context {
      override def before: Any = {
        super.before
        val values = Seq(
          Urn("soundcloud", "users", "12490957"),
          Urn("soundcloud", "users", "100")
        )
        followsMock.mutualFollowings(session, Urn("soundcloud", "users", "999"), Urn("soundcloud", "users", "2")) returns Future
          .value(Some(UserUrns(values)))
        okidokiMock.fetch(session, values.toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
        followCountsClientMock.counts(session, values) returns Future.value(Seq(FollowCounts(values.last, 1111, 2222)))
        repostsClientMock.getRepostCountsByUrnWithFallback(session, values.toSet) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response =
        get(handler.fetchMutualFollowings, "/users/999/followings/common_to/2", Map("limit" -> "10", "cursor" -> "2"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj("collection" -> List(anotherUser123))
    }
  }

  "GET /me/followings/ids" >> {
    "fetches a user's followings" in new Context {
      override def before: Any = {
        super.before
        val values = Seq(
          Following(
            "123-123",
            new DateTime("2012-02-13T23:30:13.000"),
            Urn("soundcloud", "users", "12490957"),
            Urn("soundcloud", "users", "100")
          )
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followings(session, session.getUser, None, 10) returns Future.value(
          Some(FollowingsPage(values, Some(pageInfo)))
        )
        okidokiMock.fetch(session, values.map(_.target).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
        followCountsClientMock.counts(session, values.map(_.target)) returns Future.value(
          Seq(FollowCounts(values.map(_.target).last, 1111, 2222))
        )
        repostsClientMock.getRepostCountsByUrnWithFallback(session, values.map(_.target).toSet) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response = get(handler.fetchFollowings, "/me/followings/ids", Map("limit" -> "10"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj(
        "collection" -> List(123),
        "next_href" -> "http://foo/me/followings/ids?page_size=2&cursor=123-1234"
      )
    }
  }

  "GET /me/followers/ids" >> {
    "fetches a user's followings" in new Context {
      override def before: Any = {
        super.before
        val values = Seq(
          Following(
            "123-123",
            new DateTime("2012-02-13T23:30:13.000"),
            Urn("soundcloud", "users", "12490957"),
            Urn("soundcloud", "users", "100")
          )
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followers(session, session.getUser, None, 10) returns Future.value(
          Some(FollowingsPage(values, Some(pageInfo)))
        )
        okidokiMock.fetch(session, values.map(_.user).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
        followCountsClientMock.counts(session, values.map(_.user)) returns Future.value(
          Seq(FollowCounts(values.map(_.user).last, 1111, 2222))
        )
        repostsClientMock.getRepostCountsByUrnWithFallback(session, values.map(_.user).toSet) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response = get(handler.fetchMyFollowerIds, "/me/followers/ids", Map("limit" -> "10"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj(
        "collection" -> List(123),
        "next_href" -> "http://foo/me/followers/ids?page_size=2&cursor=123-1234"
      )
    }
  }

  "GET /me/followings" >> {
    trait FollowingsContext extends Context {
      val followings = Seq(
        Following("123-123", new DateTime("2012-02-13T23:30:13.000"), Urn("soundcloud", "users", "123"), userUrn)
      )

      override def before: Any = {
        super.before
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followings(session, session.getUser, None, 10) returns Future.value(
          Some(FollowingsPage(followings, Some(pageInfo)))
        )
        okidokiMock.fetch(session, followings.map(_.target).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
      }
    }

    "fetches a user's followings" >> {
      "with follow counts flag on" in new FollowingsContext {
        override def before: Any = {
          super.before
          followCountsClientMock.counts(session, followings.map(_.target)) returns Future.value(
            Seq(FollowCounts(followings.head.target, 1111, 2222))
          )
          repostsClientMock.getRepostCountsByUrnWithFallback(session, followings.map(_.target).toSet) returns Future
            .value(Map.empty[Urn, Long])
        }

        val response = get(handler.fetchFollowings, "/me/followings", Map("limit" -> "10", "client_id" -> "FOO"))
        response.status ==== Status.Ok
        Json.parse(response.contentString) ==== Json.obj(
          "collection" -> List(user123),
          "next_href" -> "http://foo/me/followings?client_id=FOO&page_size=2&cursor=123-1234"
        )
      }
    }
  }

  "GET /me/followers" >> {
    "fetches a user's followers" in new Context {
      override def before: Any = {
        super.before
        val values = Seq(
          Following(
            "123-123",
            new DateTime("2012-02-13T23:30:13.000"),
            Urn("soundcloud", "users", "12490957"),
            Urn("soundcloud", "users", "100")
          )
        )
        val pageInfo = Pagination("123-1234", 2)
        followsMock.followers(session, session.getUser, Some("foo"), 10) returns Future.value(
          Some(FollowingsPage(values, Some(pageInfo)))
        )
        okidokiMock.fetch(session, values.map(_.user).toSet) returns Future.value(okidokiUsers.as[List[JsObject]])
        followCountsClientMock.counts(session, values.map(_.user)) returns Future.value(
          Seq(FollowCounts(values.map(_.user).last, 1111, 2222))
        )
        repostsClientMock.getRepostCountsByUrnWithFallback(session, values.map(_.user).toSet) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response = get(handler.fetchMyFollowers, "/me/followers", Map("limit" -> "10", "cursor" -> "foo"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj(
        "collection" -> List(anotherUser123),
        "next_href" -> "http://foo/me/followers?page_size=2&cursor=123-1234"
      )
    }
  }

  trait FetchesFollowingContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud", "users", "123")
      val filteredUserUrns = FilteredUserUrns(included = Set(candidateUser), excluded = Set.empty)

      followsMock.filterFollowings(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set(candidateUser)) returns Future.value(okidokiUsers.as[List[JsObject]])
      followCountsClientMock.counts(session, Seq(candidateUser)) returns Future.value(
        Seq(FollowCounts(candidateUser, 1111, 2222))
      )
      repostsClientMock.getRepostCountsByUrnWithFallback(session, Set(candidateUser)) returns Future.value(
        Map.empty[Urn, Long]
      )
    }
  }

  trait FollowingNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud", "users", "123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowings(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
      followCountsClientMock.counts(session, Seq.empty) returns Future.value(Seq.empty)
      repostsClientMock.getRepostCountsByUrnWithFallback(session, Set.empty) returns Future.value(Map.empty[Urn, Long])
    }
  }

  "GET /me/followings/:other_id" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(handler.fetchPossibleFollower, "/me/followings/123")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")
      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(handler.fetchPossibleFollower, "/me/followings/123")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  "GET /me/followings/:other_id.json" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(handler.fetchPossibleFollower, "/me/followings/123.json")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")
      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(handler.fetchPossibleFollower, "/me/followings/123.json")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  "GET /users/:id/followings/:other_id" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(handler.fetchPossibleFollowingWithoutAuth, "/users/999/followings/123")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")
      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(handler.fetchPossibleFollowingWithoutAuth, "/users/999/followings/123")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  "GET /users/:id/followings/:other_id.json" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get(handler.fetchPossibleFollowingWithoutAuth, "/users/999/followings/123.json")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")
      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get(handler.fetchPossibleFollowingWithoutAuth, "/users/999/followings/123.json")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  trait FetchesFollowerContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud", "users", "123")
      val filteredUserUrns = FilteredUserUrns(included = Set(candidateUser), excluded = Set.empty)

      followsMock.filterFollowers(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set(candidateUser)) returns Future.value(okidokiUsers.as[List[JsObject]])
      followCountsClientMock.counts(session, Seq(candidateUser)) returns Future.value(
        Seq(FollowCounts(candidateUser, 1111, 2222))
      )
      repostsClientMock.getRepostCountsByUrnWithFallback(session, Set(candidateUser)) returns Future.value(
        Map.empty[Urn, Long]
      )
    }
  }

  trait FollowerNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud", "users", "123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowers(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
      followCountsClientMock.counts(session, Seq.empty) returns Future.value(Seq.empty)
      repostsClientMock.getRepostCountsByUrnWithFallback(session, Set.empty) returns Future.value(Map.empty[Urn, Long])
    }
  }

  "GET /me/followers/:other_id" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(handler.fetchPossibleFollower, "/me/followers/123")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")

      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(handler.fetchPossibleFollower, "/me/followers/123")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  "GET /me/followers/:other_id.json" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(handler.fetchPossibleFollower, "/me/followers/123.json")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")

      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(handler.fetchPossibleFollower, "/me/followers/123.json")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  "GET /users/:id/followers/:other_id" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(handler.fetchPossibleFollowerWithoutAuth, "/users/999/followers/123")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")

      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(handler.fetchPossibleFollowerWithoutAuth, "/users/999/followers/123")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  "GET /users/:id/followers/:other_id.json" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get(handler.fetchPossibleFollowerWithoutAuth, "/users/999/followers/123.json")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")

      Json.parse(response.contentString) ==== user123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get(handler.fetchPossibleFollowerWithoutAuth, "/users/999/followers/123.json")
      response.status ==== Status.NotFound
      response.contentString ==== ""
    }
  }

  "PUT /me/followings/:other_id.json" >> {
    "works like the route without .json" in new Context {
      override def before: Any = {
        super.before
        val following = Following("1", DateTime.now, userUrn, Urn("soundcloud", "users", "999"))
        followsMock.follow(session, userUrn) returns Future.value(FollowingCreated(following))
        followCountsClientMock.counts(session, Seq(following.target)) returns Future.value(
          Seq(FollowCounts(following.target, 1111, 2222))
        )
        repostsClientMock.getRepostCountsByUrnWithFallback(session, Set(following.target)) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response = put(handler.follow, "/me/followings/999.json", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Created
      Json.parse(response.contentString) ==== anotherUser123
    }
  }

  "PUT /me/followings/:other_id" >> {
    "follows a profile" in new Context {
      override def before: Any = {
        super.before
        val following = Following("1", DateTime.now, userUrn, Urn("soundcloud", "users", "999"))
        followsMock.follow(session, userUrn) returns Future.value(FollowingCreated(following))

        followCountsClientMock.counts(session, Seq(following.target)) returns Future.value(
          Seq(FollowCounts(following.target, 1111, 2222))
        )
        repostsClientMock.getRepostCountsByUrnWithFallback(session, Set(following.target)) returns Future.value(
          Map.empty[Urn, Long]
        )
      }

      val response = put(handler.follow, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Created
      Json.parse(response.contentString) ==== anotherUser123
    }

    "render the age-restricted errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(AgeRestrictedUser)
      }

      val response = put(handler.follow, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Forbidden
      val errors = (Json.parse(response.contentString) \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(35)
    }

    "render the age-unknown errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(AgeUnknownUser)
      }

      val response = put(handler.follow, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Forbidden
      val errors = (Json.parse(response.contentString) \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_UNKNOWN")
    }

    "render regular errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(UserNotFound)
      }

      val response = put(handler.follow, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.NotFound
      val errors = (Json.parse(response.contentString) \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("404 - Not Found")
    }
  }

  "DELETE /me/followings/:other_id.json" >> {
    "works like the route without .json" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UnfollowSuccessful)
      }

      val response = delete(handler.unfollow, "/me/followings/999.json", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }
  }

  "DELETE /me/followings/:other_id" >> {
    "unfollows a profile" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UnfollowSuccessful)
      }

      val response = delete(handler.unfollow, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.Ok
    }

    "render errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UserAsTarget)
      }

      val response = delete(handler.unfollow, "/me/followings/999", Map("client_id" -> "YOUR_CLIENT_ID"))
      response.status ==== Status.UnprocessableEntity
    }
  }
}
