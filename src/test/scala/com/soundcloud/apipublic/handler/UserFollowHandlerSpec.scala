package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.follows.FollowsClient
import com.soundcloud.apipublic.client.follows.representation._
import com.soundcloud.apipublic.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.apipublic.client.follows.representation.follow.{
  AgeRestrictedUser,
  AgeUnknownUser,
  FollowingCreated,
  UserNotFound
}
import com.soundcloud.apipublic.client.follows.representation.unfollow.{UnfollowSuccessful, UserAsTarget}
import com.soundcloud.apipublic.client.mothership.OkidokiClient
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.soundcloud.apipublic.test.fixtures.Fixtures._
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeUtils, LocalDate, Years}
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._

class UserFollowHandlerSpec extends UnitSpecification {
  sequential

  trait Context extends HandlerSpecificationScope with BeforeAfter {
    val okidokiMock = mock[OkidokiClient]
    val followsMock = mock[FollowsClient]

    val userUrn = Urn("soundcloud", "users", "999")
    val userServiceMock = mock[UserRepresentationsService]
    lazy val geo = new Geo("US")
    lazy val session =
      new UserSessionBuilder().setUser(userUrn).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()
    private val baseUrl = "http://foo"
    lazy val handler = new UserFollowHandler(
      new FakeUserAuthentication(session),
      okidokiMock,
      followsMock,
      userServiceMock,
      baseUrl
    )

    override def routingDefinitions = Routing.forUserFollowHandler(handler)

    lazy val userMock = okidokiUsers.as[List[JsObject]].head
    lazy val okidokiResponse = Future(List(userMock))

    val now = System.currentTimeMillis()

    val user123 = Json.parse("""
        |{
        |  "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
        |  "id": 123,
        |  "urn": "soundcloud:users:123",
        |  "kind": "user",
        |  "permalink_url": "http://soundcloud.com/adeline",
        |  "uri": "https://api.soundcloud.com/users/123",
        |  "username": "adeline",
        |  "permalink": "adeline",
        |  "created_at": "2007/09/12 00:06:00 +0000",
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
        |  "comments_count": 0,
        |  "online": false,
        |  "likes_count": 5,
        |  "playlist_count": null,
        |  "subscriptions": [{"product":{"id":"pro-plus","name":"Pro Plus"}}]
        |}
      """.stripMargin)

    val anotherUser123 = Json.parse(
      """
        |{
        |  "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
        |  "id": 123,
        |  "urn": "soundcloud:users:123",
        |  "kind": "user",
        |  "permalink_url": "http://soundcloud.com/adeline",
        |  "uri": "https://api.soundcloud.com/users/123",
        |  "username": "adeline",
        |  "permalink": "adeline",
        |  "created_at": "2007/09/12 00:06:00 +0000",
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
        |  "comments_count": 0,
        |  "online": false,
        |  "likes_count": 5,
        |  "playlist_count": null,
        |  "subscriptions": [{"product":{"id":"pro-plus","name":"Pro Plus"}}]
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
          val userRepresentation = UserRepresentationMapper(Fixtures.okidokiUsers.as[JsArray].value.last)
          userServiceMock.users(session, followings.map(_.target).toSeq, false) returns Future.value(
            List(userRepresentation)
          )
        }

        val response = get("/me/followings", Map("limit" -> "10"))
        response.status ==== Status.Ok

        Json.parse(response.contentString) ==== Json.obj(
          "collection" -> List(anotherUser123),
          "next_href" -> "http://foo/me/followings?cursor=123-1234&page_size=2"
        )
      }

      "cut client_id out of next_href if present" in new FollowingsContext {
        override def before: Any = {
          super.before
          val userRepresentation = UserRepresentationMapper(Fixtures.okidokiUsers.as[JsArray].value.last)
          userServiceMock.users(session, followings.map(_.target).toSeq, false) returns Future.value(
            List(userRepresentation)
          )
        }

        val response = get("/me/followings", Map("limit" -> "10", "client_id" -> "FOO"))
        response.status ==== Status.Ok
        Json.parse(response.contentString) ==== Json.obj(
          "collection" -> List(anotherUser123),
          "next_href" -> "http://foo/me/followings?cursor=123-1234&page_size=2"
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
        val userRepresentation = UserRepresentationMapper(Fixtures.okidokiUsers.as[JsArray].value.last)
        userServiceMock.users(session, values.map(_.user).toSeq, false) returns Future.value(List(userRepresentation))
      }

      val response = get("/me/followers", Map("limit" -> "10", "cursor" -> "foo"))
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj(
        "collection" -> List(anotherUser123),
        "next_href" -> "http://foo/me/followers?cursor=123-1234&page_size=2"
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
      val userRepresentation = UserRepresentationMapper(Fixtures.okidokiUsers.as[JsArray].value.last)
      userServiceMock.users(session, Seq(candidateUser), false) returns Future.value(List(userRepresentation))
    }
  }

  trait FollowingNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud", "users", "123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowings(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
      userServiceMock.users(session, Seq.empty, false) returns Future.value(List.empty)
    }
  }

  "GET /me/followings/:other_id" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get("/me/followings/123")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")
      Json.parse(response.contentString) ==== anotherUser123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get("/me/followings/123")
      response.status ==== Status.NotFound
    }
  }

  "GET /users/:id/followings/:other_id" >> {
    "fetches a following" in new FetchesFollowingContext {
      val response = get("/users/999/followings/123")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")
      Json.parse(response.contentString) ==== anotherUser123
    }

    "returns not found when the given user is not a following" in new FollowingNotFoundContext {
      val response = get("/users/999/followings/123")
      response.status ==== Status.NotFound
    }
  }

  trait FetchesFollowerContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud", "users", "123")
      val filteredUserUrns = FilteredUserUrns(included = Set(candidateUser), excluded = Set.empty)

      followsMock.filterFollowers(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set(candidateUser)) returns Future.value(okidokiUsers.as[List[JsObject]])
      val userRepresentation = UserRepresentationMapper(Fixtures.okidokiUsers.as[JsArray].value.last)
      userServiceMock.users(session, Seq(candidateUser), false) returns Future.value(List(userRepresentation))
    }
  }

  trait FollowerNotFoundContext extends Context {
    override def before: Any = {
      super.before

      val candidateUser = Urn("soundcloud", "users", "123")
      val filteredUserUrns = FilteredUserUrns(included = Set.empty, excluded = Set(candidateUser))

      followsMock.filterFollowers(session, userUrn, Seq(candidateUser)) returns Future.value(Some(filteredUserUrns))
      okidokiMock.fetch(session, Set.empty) returns Future.value(List.empty)
      userServiceMock.users(session, Seq.empty, false) returns Future.value(List.empty);
    }
  }

  "GET /me/followers/:other_id" >> {
    "fetches a follower" in new FetchesFollowerContext {
      val response = get("/me/followers/123")
      response.status ==== Status.SeeOther
      response.headerMap.get("Location") ==== Some("http://foo/users/123")

      Json.parse(response.contentString) ==== anotherUser123
    }

    "returns not found when the given user is not a follower" in new FollowerNotFoundContext {
      val response = get("/me/followers/123")
      response.status ==== Status.NotFound
    }
  }

  "PUT /me/followings/:other_id" >> {
    "follows a profile" in new Context {
      override def before: Any = {
        super.before
        val following = Following("1", DateTime.now, userUrn, Urn("soundcloud", "users", "999"))
        followsMock.follow(session, userUrn) returns Future.value(FollowingCreated(following))

        val userRepresentation = UserRepresentationMapper(Fixtures.okidokiUsers.as[JsArray].value.last)
        userServiceMock.users(session, Seq(following.target), false) returns Future.value(List(userRepresentation))

      }

      val response = put("/me/followings/999")
      response.status ==== Status.Created
      Json.parse(response.contentString) ==== anotherUser123
    }

    "render the age-restricted errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(AgeRestrictedUser)
      }

      val response = put("/me/followings/999")
      response.status ==== Status.Forbidden
      val errors = (Json.parse(response.contentString) \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_RESTRICTED")
      (errors \ "age").asOpt[Long] ==== Option(
        Years.yearsBetween(LocalDate.parse("1984-12-01"), new LocalDate()).getYears
      )
    }

    "render the age-unknown errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(AgeUnknownUser)
      }

      val response = put("/me/followings/999")
      response.status ==== Status.Forbidden
      val errors = (Json.parse(response.contentString) \ "errors").as[Seq[JsObject]].head
      (errors \ "error_message").asOpt[String] ==== Option("DENY_AGE_UNKNOWN")
    }

    "render regular errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.follow(session, userUrn) returns Future.value(UserNotFound)
      }

      val response = put("/me/followings/999")
      response.status ==== Status.NotFound
      (Json.parse(response.contentString) \ "status").get === JsString("404 - Not Found")
    }
  }

  "DELETE /me/followings/:other_id" >> {
    "unfollows a profile" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UnfollowSuccessful)
      }

      val response = delete("/me/followings/999")
      response.status ==== Status.Ok
    }

    "render errors" in new Context {
      override def before: Any = {
        super.before
        followsMock.unfollow(session, userUrn) returns Future.value(UserAsTarget)
      }

      val response = delete("/me/followings/999")
      response.status ==== Status.UnprocessableEntity
    }
  }
}
