package com.soundcloud.apipublic.client.follows

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Geo, Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.apipublic.client.follows.representation._
import com.soundcloud.apipublic.client.follows.representation.follow._
import com.soundcloud.apipublic.client.follows.representation.unfollow.{
  UnfollowSuccessful,
  NotFollowing => UnfollowNotFollowing,
  UnknownError => UnfollowUnknownError,
  UserAsTarget => UnfollowUserAsTarget,
  UserNotFound => UnfollowUserNotFound
}
import com.soundcloud.apipublic.client.chrono.ChronoItem
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime
import play.api.libs.json.{JsNull, JsString, JsValue, Json}
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.Status

import scala.io.{Codec, Source}
import org.mockito.Mockito.{verify, when}
import org.specs2.mutable.After

object Fixtures {
  lazy val emptyFollowingsPage = fileJson("empty_followings_page")
  lazy val oneFollowingPage = fileJson("one_following_page")
  lazy val oneFollowingPageWithNext = fileJson("one_following_page_with_next")
  lazy val filteredUserUrns = fileJson("filtered_user_urns")
  lazy val emptyUserUrns = fileJson("empty_user_urns")
  lazy val userUrns = fileJson("user_urns")
  lazy val followingCreated = fileJson("following_created")
  lazy val maxFollowingsReachedError = fileJson("max_followings_reached_error")
  lazy val userAsTargetError = fileJson("user_as_target_error")
  lazy val ageRestrictedUserError = fileJson("age_restricted_user_error")
  lazy val ageUnknownUserError = fileJson("age_unknown_user_error")
  lazy val notFollowingError = fileJson("not_following_error")
  lazy val followingsChronoResponse = Json.parse("""
      {
        "items": [
          {
            "timestamp": "2026-02-20T12:00:00.000Z",
            "type": "follow",
            "urn": "soundcloud:users:2",
            "cursor": "2026-02-20T12:00:00.000Z,source:follow,00000000041170609376"
          }
        ],
        "meta": {
          "params": {
            "direction": "asc",
            "limit": 20
          },
          "valid_for_caching": true
        }
      }
    """)

  private def fileJson(name: String) = Json.parse(fileToString(name))

  private def fileToString(name: String) =
    Source.fromURL(getClass.getResource(s"/fixtures/follows/$name.json"))(Codec.UTF8).mkString
}

class FollowsClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val user = Urn("soundcloud", "users", "1")
    val anotherUser = Urn("soundcloud", "users", "2")
    val yetAnotherUser = Urn("soundcloud", "users", "3")
    val dateTime = new DateTime("2015-12-08T00:32:10.000+00:00")

    val serviceMock = mock[JsonClient]
    val client = new FollowsClient(serviceMock)
  }

  "#follow" >> {
    trait FollowContext extends Context with After {
      val path = Path() / "follow" / anotherUser

      lazy val geo = new Geo("US")
      lazy val session =
        new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()

      lazy val result = Await.result(client.follow(session, anotherUser))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.postWithSession(session, path, Params.empty, Headers.empty, None))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).postWithSession(session, path, Params.empty, Headers.empty, None)
      }
    }

    "returns the created following when a new following is created" in new FollowContext {
      mockWith(Status.Created, Fixtures.followingCreated)

      result ==== FollowingCreated(Following("42", dateTime, anotherUser, user))
    }

    "indicates when the target user is already being followed" in new FollowContext {
      mockWith(Status.Ok, JsNull)

      result ==== AlreadyFollowing
    }

    "indicates when the target user doesn'dateTime exist" in new FollowContext {
      mockWith(Status.NotFound, JsNull)

      result ==== UserNotFound
    }

    "indicates when the user is blocked for spam" in new FollowContext {
      mockWith(Status.TooManyRequests, JsNull)

      result ==== SpamBlocked
    }

    "indicates when the user is blocked by the target user" in new FollowContext {
      mockWith(Status.Forbidden, JsNull)

      result ==== BlockedByTarget
    }

    "indicates when the user has reached the maximum possible followings" in new FollowContext {
      mockWith(Status.UnprocessableEntity, Fixtures.maxFollowingsReachedError)

      result ==== MaxFollowingsReached
    }

    "indicates rejection reasons it does not recognize" in new FollowContext {
      mockWith(
        Status.UnprocessableEntity,
        Json.obj("error" -> Json.obj("name" -> "SomeNewRejection", "message" -> "some new reason"))
      )

      result ==== FollowingRejected("SomeNewRejection")
    }

    "indicates when the user tries to follow themselves" in new FollowContext {
      mockWith(Status.UnprocessableEntity, Fixtures.userAsTargetError)

      result ==== UserAsTarget
    }

    "indicates when the user is underage and tries to follow an age restricted target user" in new FollowContext {
      mockWith(Status.UnprocessableEntity, Fixtures.ageRestrictedUserError)

      result ==== AgeRestrictedUser
    }

    "indicates when the user age is unknown and tries to follow an age restricted target user" in new FollowContext {
      mockWith(Status.UnprocessableEntity, Fixtures.ageUnknownUserError)

      result ==== AgeUnknownUser
    }

    "treats underspecified errors as unknown errors" in new FollowContext {
      mockWith(Status.UnprocessableEntity, JsString("Some weird message"))

      result ==== UnknownError("Unknown error: \"Some weird message\"", 422)
    }

    "handles unknown errors" in new FollowContext {
      mockWith(Status.InternalServerError, JsNull)

      result ==== UnknownError("Unknown error: null", 500)
    }
  }

  "#bulkFollow" >> {
    trait FollowCotext extends Context with After {
      val path = Path() / "bulkfollow"

      lazy val geo = new Geo("US")
      lazy val session =
        new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()

      lazy val result = Await.result(client.bulkFollow(session, List(anotherUser)))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.postWithSession(session, path, Params("urns" -> List(anotherUser)), Headers.empty, None))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).postWithSession(session, path, Params("urns" -> List(anotherUser)), Headers.empty, None)
      }
    }

    "returns the target when following is successful" in new FollowCotext {
      mockWith(Status.Created, JsNull)

      result.head.isInstanceOf[FollowingCreated]
    }

    "returns failure when following fails" in new FollowCotext {
      mockWith(Status.BadRequest, JsString("error"))

      result ==== List(BulkFollowFailed(List(anotherUser)))
    }

    "forwards error code when following fails with unknown error" in new FollowCotext {
      mockWith(Status.UnprocessableEntity, JsString("error"))

      result.asInstanceOf[List[UnknownError]].head.status ==== 422
    }
  }

  "#unfollow" >> {
    trait UnfollowCotext extends Context with After {
      val path = Path() / "unfollow" / anotherUser

      lazy val geo = new Geo("US")
      lazy val session =
        new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()

      lazy val result = Await.result(client.unfollow(session, anotherUser))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.deleteWithSession(session, path, Params.empty, Headers.empty, None))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).deleteWithSession(session, path, Params.empty, Headers.empty, None)
      }
    }

    "returns the success of an unfollowing" in new UnfollowCotext {
      mockWith(Status.Ok, JsNull)

      result ==== UnfollowSuccessful
    }

    "indicates when the target user to unfollow doesn't exist" in new UnfollowCotext {
      mockWith(Status.NotFound, JsNull)

      result ==== UnfollowUserNotFound
    }

    "indicates when the user is the target" in new UnfollowCotext {
      mockWith(Status.UnprocessableEntity, Fixtures.userAsTargetError)

      result ==== UnfollowUserAsTarget
    }

    "indicates when the target user is not being followed" in new UnfollowCotext {
      mockWith(Status.UnprocessableEntity, Fixtures.notFollowingError)

      result ==== UnfollowNotFollowing
    }

    "handles unknown errors" in new UnfollowCotext {
      mockWith(Status.InternalServerError, JsNull)

      result ==== UnfollowUnknownError("Unknown error: null", 500)
    }
  }

  "#followings" >> {
    trait FollowingsContext extends Context with After {
      val path = Path() / "users" / user / "followings"
      val params = Params("last_id" -> "12345", "page_size" -> 1)

      lazy val result = Await.result(client.followings(anonymousSession, user, Some("12345"), 1))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.getWithSession(anonymousSession, path, params, Headers.empty))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).getWithSession(anonymousSession, path, params, Headers.empty)
      }
    }

    "returns none when an error happens" in new FollowingsContext {
      mockWith(Status.InternalServerError, JsNull)

      result ==== None
    }

    "returns an empty page when no followings are found" in new FollowingsContext {
      mockWith(Status.Ok, Fixtures.emptyFollowingsPage)

      result ==== Some(FollowingsPage(Seq.empty, None))
    }

    "returns the followings when one page of results is found" in new FollowingsContext {
      mockWith(Status.Ok, Fixtures.oneFollowingPage)

      val followings = Seq(Following("42", dateTime, anotherUser, user))
      val pagination = None

      result ==== Some(FollowingsPage(followings, pagination))
    }

    "returns the followings and the next page information when multiple pages are found" in new FollowingsContext {
      mockWith(Status.Ok, Fixtures.oneFollowingPageWithNext)

      val followings = Seq(Following("42", dateTime, anotherUser, user))
      val pagination = Some(Pagination("12345", 1))

      result ==== Some(FollowingsPage(followings, pagination))
    }
  }

  "#followers" >> {
    trait FollowersContext extends Context with After {
      val path = Path() / "users" / anotherUser / "followers"
      val params = Params("last_id" -> "12345", "page_size" -> 1)

      lazy val result = Await.result(client.followers(anonymousSession, anotherUser, Some("12345"), 1))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.getWithSession(anonymousSession, path, params, Headers.empty))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).getWithSession(anonymousSession, path, params, Headers.empty)
      }
    }

    "returns none when an error happens" in new FollowersContext {
      mockWith(Status.InternalServerError, JsNull)

      result ==== None
    }

    "returns an empty page when no followings are found" in new FollowersContext {
      mockWith(Status.Ok, Fixtures.emptyFollowingsPage)

      result ==== Some(FollowingsPage(Seq.empty, None))
    }

    "returns the followings when one page of results is found" in new FollowersContext {
      mockWith(Status.Ok, Fixtures.oneFollowingPage)

      val followings = Seq(Following("42", dateTime, anotherUser, user))
      val pagination = None

      result ==== Some(FollowingsPage(followings, pagination))
    }

    "returns the followings and the next page information when multiple pages are found" in new FollowersContext {
      mockWith(Status.Ok, Fixtures.oneFollowingPageWithNext)

      val followings = Seq(Following("42", dateTime, anotherUser, user))
      val pagination = Some(Pagination("12345", 1))

      result ==== Some(FollowingsPage(followings, pagination))
    }
  }

  "#followingsChrono" >> {
    trait FollowingsChronoContext extends Context with After {
      val path = Path() / "users" / anotherUser / "followings" / "chrono"
      val params = Params("limit" -> 20, "direction" -> "asc")

      lazy val result = Await.result(client.followingsChrono(anonymousSession, anotherUser, None, 20, "asc"))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.getWithSession(anonymousSession, path, params, Headers.empty))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).getWithSession(anonymousSession, path, params, Headers.empty)
      }
    }

    "returns none when an error happens" in new FollowingsChronoContext {
      mockWith(Status.InternalServerError, JsNull)

      result ==== None
    }

    "returns empty response when no followings are found" in new FollowingsChronoContext {
      mockWith(
        Status.Ok,
        Json.obj(
          "items" -> Json.arr(),
          "meta" -> Json.obj("params" -> Json.obj("direction" -> "asc", "limit" -> 20), "valid_for_caching" -> true)
        )
      )

      result.map(_.items) ==== Some(List.empty)
    }

    "returns chrono response when followings are found" in new FollowingsChronoContext {
      mockWith(Status.Ok, Fixtures.followingsChronoResponse)

      val expectedItem = ChronoItem(
        "2026-02-20T12:00:00.000Z",
        "follow",
        anotherUser,
        "2026-02-20T12:00:00.000Z,source:follow,00000000041170609376"
      )
      result.map(_.items) ==== Some(List(expectedItem))
      result.map(_.meta.params.direction) ==== Some("asc")
      result.map(_.meta.params.limit) ==== Some(20)
    }

    "sends cursor when provided" in new Context with After {
      val path = Path() / "users" / anotherUser / "followings" / "chrono"
      val params = Params("limit" -> 20, "direction" -> "asc", "cursor" -> "some-cursor")
      when(serviceMock.getWithSession(anonymousSession, path, params, Headers.empty))
        .thenReturn(Future(jsonResponse(Status.Ok, Fixtures.followingsChronoResponse)))
      val result = Await.result(client.followingsChrono(anonymousSession, anotherUser, Some("some-cursor"), 20, "asc"))
      result.map(_.items.size) ==== Some(1)
      override def after: Any = verify(serviceMock).getWithSession(anonymousSession, path, params, Headers.empty)
    }
  }

  "#filterFollowings" >> {
    trait FilterFollowingsContext extends Context with After {
      val included = Seq(anotherUser)
      val excluded = Seq(yetAnotherUser)
      val path = Path() / "users" / user / "filter_followings"
      val params = Params("urns" -> (included ++ excluded))

      lazy val result = Await.result(client.filterFollowings(anonymousSession, user, included ++ excluded))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.getWithSession(anonymousSession, path, params, Headers.empty))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).getWithSession(anonymousSession, path, params, Headers.empty)
      }
    }

    "returns none when an error happens" in new FilterFollowingsContext {
      mockWith(Status.InternalServerError, JsNull)

      result ==== None
    }

    "returns the urns split in two" in new FilterFollowingsContext {
      mockWith(Status.Ok, Fixtures.filteredUserUrns)

      result ==== Some(FilteredUserUrns(included.toSet, excluded.toSet))
    }
  }

  "#filterFollowers" >> {
    trait FilterFollowersContext extends Context with After {
      val included = Seq(anotherUser)
      val excluded = Seq(yetAnotherUser)
      val path = Path() / "users" / user / "filter_followers"
      val params = Params("urns" -> (included ++ excluded))

      lazy val result = Await.result(client.filterFollowers(anonymousSession, user, included ++ excluded))

      def mockWith(status: Status, body: JsValue) =
        when(serviceMock.getWithSession(anonymousSession, path, params, Headers.empty))
          .thenReturn(Future(jsonResponse(status, body)))

      override def after: Any = {
        verify(serviceMock).getWithSession(anonymousSession, path, params, Headers.empty)
      }
    }

    "returns none when an error happens" in new FilterFollowersContext {
      mockWith(Status.InternalServerError, JsNull)

      result ==== None
    }

    "returns the urns split in two" in new FilterFollowersContext {
      mockWith(Status.Ok, Fixtures.filteredUserUrns)

      result ==== Some(FilteredUserUrns(included.toSet, excluded.toSet))
    }
  }
}
