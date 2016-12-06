package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.test.FakeUserAuthentication
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.twitter.finagle.http.{Request => FinagleRequest}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{never, verify, when}
import org.specs2.mutable.{After, Before, BeforeAfter}
import play.api.libs.json._

class UserRelatedMothershipDispatcherSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope with BeforeAfter {
    val session = mock[UserSession]
    val userAuthenticationMock = new FakeUserAuthentication(session)
    val followCountsClientMock = mock[FollowCountsClient]
    val lieblingClientMock = mock[LieblingClient]
    val mothershipDispatcherMock = mock[DispatchToMothershipHandler]
    val request = new Request(mock[FinagleRequest])

    def loadUserLikeCountsFromLiebling: Boolean

    val dispatcher = new UserRelatedMothershipDispatcher(
      userAuthenticationMock,
      mothershipDispatcherMock,
      followCountsClientMock,
      lieblingClientMock,
      () => Future.value(loadUserLikeCountsFromLiebling)
    )

    val user1 = new Urn("soundcloud", "users", "183")
    val user2 = new Urn("soundcloud", "users", "1111")
    val user3 = new Urn("soundcloud", "users", "2222")

    def responseStatusFromPublicApi: Int = 200

    def responseBodyFromPublicApi: JsValue

    def responseBuilder = new ResponseBuilder()
      .status(responseStatusFromPublicApi)
      .body(responseBodyFromPublicApi.toString())

    def userUrns: Seq[Urn] = Seq.empty
    def followCountsSeq: Seq[FollowCounts] = Seq.empty

    def result = Await.result(dispatcher.dispatchToMothership(request)).build
    lazy val resultJson = Json.parse(result.getContentString())

    override def before: Any = {
      when(mothershipDispatcherMock.defaultHandling(any[HandlerRequest])).thenReturn(Future.value(responseBuilder.build))
    }

    override def after: Any = {}
  }

  "when the feature flag to load user like counts from liebling is true" >> {
    trait LoadUserLikeCountsFromLieblingIsTrue extends Context {
      override def loadUserLikeCountsFromLiebling = true

      def userTotalLikesList: List[UserTotalLikes] = List.empty

      override def before: Any = {
        super.before

        when(followCountsClientMock.counts(session, userUrns)).thenReturn(Future.value(followCountsSeq))
        when(lieblingClientMock.userTotalLikeCount(session, userUrns)).thenReturn(Future.value(userTotalLikesList))
      }
    }

    "follow counts" >> {
      "with a non-JSON response" in new LoadUserLikeCountsFromLieblingIsTrue {
        override def responseBodyFromPublicApi = JsNull

        override def responseBuilder = new ResponseBuilder()
          .status(responseStatusFromPublicApi)
          .body("No a JSON response")

        Await.result(dispatcher.dispatchToMothership(request)).build.getContentString() ==== "No a JSON response"
      }

      "with a non-OK status code" in new LoadUserLikeCountsFromLieblingIsTrue {
        override def responseStatusFromPublicApi = 500

        override def responseBodyFromPublicApi = user

        result.getStatusCode() ==== 500
      }

      "with an OK status code" >> {
        "with follow and like counts which are the same as returned by public api" >> {
          // this test is here to ensure that we simply enrich the representation returned
          // by the public api with the counts, as making that assertion in the other tests
          // was very verbose and difficult to read
          "it returns an object which is the same than the public api" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = user
            override def userUrns = Seq(user1)
            override def followCountsSeq = Seq(FollowCounts(user1, 100, 101))
            override def userTotalLikesList = List(UserTotalLikes(user1, 100, 23))

            resultJson ==== responseBodyFromPublicApi
          }
        }

        "with no follow or like counts from clients" >> {
          "with single object" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = user

            override def userUrns = Seq(user1)

            (resultJson \ "followers_count").as[Long] ==== 0
            (resultJson \ "followings_count").as[Long] ==== 0
            (resultJson \ "public_favorites_count").as[Long] ==== 0
          }

          "with users in the top level" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = users

            override def userUrns = Seq(user2, user3)

            val collection = resultJson.as[JsArray].value

            (collection.head \ "followers_count").as[Long] ==== 0
            (collection.head \ "followings_count").as[Long] ==== 0
            (collection.head \ "public_favorites_count").as[Long] ==== 0

            (collection.last \ "followers_count").as[Long] ==== 0
            (collection.last \ "followings_count").as[Long] ==== 0
            (collection.last \ "public_favorites_count").as[Long] ==== 0
          }

          "with users in a collection" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = usersInCollection
            override def userUrns = Seq(user2, user3)

            val collection = (resultJson \ "collection").as[JsArray].value

            collection.size ==== 2

            (collection.head \ "followers_count").as[Long] ==== 0
            (collection.head \ "followings_count").as[Long] ==== 0
            (collection.head \ "public_favorites_count").as[Long] ==== 0

            (collection.last \ "followers_count").as[Long] ==== 0
            (collection.last \ "followings_count").as[Long] ==== 0
            (collection.last \ "public_favorites_count").as[Long] ==== 0
          }

          "with objects containing a user" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = objectsWithUsers

            override def userUrns = Seq(user2, user3)

            val collection = resultJson.as[JsArray].value

            collection.size ==== 2

            (collection.head \ "user" \ "followers_count").as[Long] ==== 0
            (collection.head \ "user" \ "followings_count").as[Long] ==== 0
            (collection.head \ "user" \ "public_favorites_count").as[Long] ==== 0

            (collection.last \ "user" \ "followers_count").as[Long] ==== 0
            (collection.last \ "user" \ "followings_count").as[Long] ==== 0
            (collection.last \ "user" \ "public_favorites_count").as[Long] ==== 0
          }
        }

        "with follow counts from client" >> {
          "with single object" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = user

            override def userUrns = Seq(user1)

            override def followCountsSeq = Seq(FollowCounts(user1, 100, 200))

            (resultJson \ "followers_count").as[Long] ==== 100
            (resultJson \ "followings_count").as[Long] ==== 200
          }

          "with users in the top level" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = users

            override def userUrns = Seq(user2, user3)

            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val collection = resultJson.as[JsArray].value

            collection.size ==== 2

            (collection.head \ "followers_count").as[Long] ==== 100
            (collection.head \ "followings_count").as[Long] ==== 200

            (collection.last \ "followers_count").as[Long] ==== 300
            (collection.last \ "followings_count").as[Long] ==== 400
          }

          "with users in a collection" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = usersInCollection

            override def userUrns = Seq(user2, user3)

            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val values = (resultJson \ "collection").as[JsArray].value

            values.size ==== 2

            (values.head \ "followers_count").as[Long] ==== 100
            (values.head \ "followings_count").as[Long] ==== 200

            (values.last \ "followers_count").as[Long] ==== 300
            (values.last \ "followings_count").as[Long] ==== 400
          }

          "with objects containing a user" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = objectsWithUsers

            override def userUrns = Seq(user2, user3)

            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val values = resultJson.as[JsArray].value

            values.size ==== 2

            (values.head \ "user" \ "followers_count").as[Long] ==== 100
            (values.head \ "user" \ "followings_count").as[Long] ==== 200

            (values.last \ "user" \ "followers_count").as[Long] ==== 300
            (values.last \ "user" \ "followings_count").as[Long] ==== 400
          }
        }

        "with like counts from client" >> {
          "with single object" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = user

            override def userUrns = Seq(user1)

            override def userTotalLikesList = List(UserTotalLikes(user1, 100, 200))

            (resultJson \ "public_favorites_count").as[Long] ==== 300
          }

          "with users in the top level" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = users

            override def userUrns = Seq(user2, user3)

            override def userTotalLikesList = List(UserTotalLikes(user2, 100, 200), UserTotalLikes(user3, 300, 400))

            val values = resultJson.as[JsArray].value

            values.size ==== 2

            (values.head \ "public_favorites_count").as[Long] ==== 300
            (values.last \ "public_favorites_count").as[Long] ==== 700
          }

          "with users in a collection" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = usersInCollection

            override def userUrns = Seq(user2, user3)

            override def userTotalLikesList = List(UserTotalLikes(user2, 100, 0), UserTotalLikes(user3, 300, 1))

            val values = (resultJson \ "collection").as[JsArray].value

            values.size ==== 2

            (values.head \ "public_favorites_count").as[Long] ==== 100
            (values.last \ "public_favorites_count").as[Long] ==== 301
          }

          "with objects containing a user" in new LoadUserLikeCountsFromLieblingIsTrue {
            override def responseBodyFromPublicApi = objectsWithUsers

            override def userUrns = Seq(user2, user3)

            override def userTotalLikesList = List(UserTotalLikes(user2, 100, 1), UserTotalLikes(user3, 300, 0))

            val values = resultJson.as[JsArray].value

            values.size ==== 2

            (values.head \ "user" \ "public_favorites_count").as[Long] ==== 101
            (values.last \ "user" \ "public_favorites_count").as[Long] ==== 300
          }
        }
      }
    }
  }

  // TODO remove after feature flag is removed
  "when the feature flag to load user like counts from liebling is false" >> {
    trait LoadUserLikeCountsFromLieblingIsFalse extends Context {
      override def loadUserLikeCountsFromLiebling = false

      override def before: Any = {
        super.before
        when(followCountsClientMock.counts(session, userUrns)).thenReturn(Future.value(followCountsSeq))
      }

      override def after: Any = {
        verify(lieblingClientMock, never()).userTotalLikeCount(session, userUrns)
      }
    }

    "follow counts" >> {
      "with a non-JSON response" in new LoadUserLikeCountsFromLieblingIsFalse {
        override def responseBodyFromPublicApi = JsNull

        override def responseBuilder = new ResponseBuilder()
          .status(responseStatusFromPublicApi)
          .body("No a JSON response")

        Await.result(dispatcher.dispatchToMothership(request)).build.getContentString() ==== "No a JSON response"
      }

      "with a non-OK status code" in new LoadUserLikeCountsFromLieblingIsFalse {
        override def responseStatusFromPublicApi = 500

        override def responseBodyFromPublicApi = user

        result.getStatusCode() ==== 500
      }

      "with an OK status code" >> {
        "with no follow counts from client" >> {
          "with single object" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = user

            override def userUrns = Seq(user1)

            (resultJson \ "followers_count").as[Long] ==== 0
            (resultJson \ "followings_count").as[Long] ==== 0
          }

          "with users in the top level" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = users

            override def userUrns = Seq(user2, user3)

            resultJson ==== responseBodyFromPublicApi
          }

          "with users in a collection" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = usersInCollection

            override def userUrns = Seq(user2, user3)

            resultJson ==== responseBodyFromPublicApi
          }

          "with objects containing a user" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = objectsWithUsers

            override def userUrns = Seq(user2, user3)

            resultJson ==== responseBodyFromPublicApi
          }
        }

        "with follow counts from client" >> {
          "with single object" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = user

            override def userUrns = Seq(user1)

            override def followCountsSeq = Seq(FollowCounts(user1, 100, 200))

            (resultJson \ "followers_count").as[Long] ==== 100
            (resultJson \ "followings_count").as[Long] ==== 200
          }

          "with users in the top level" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = users

            override def userUrns = Seq(user2, user3)

            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val values = resultJson.as[JsArray].value

            values.size ==== 2

            (values.head \ "followers_count").as[Long] ==== 100
            (values.head \ "followings_count").as[Long] ==== 200

            (values.last \ "followers_count").as[Long] ==== 300
            (values.last \ "followings_count").as[Long] ==== 400
          }

          "with users in a collection" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = usersInCollection

            override def userUrns = Seq(user2, user3)

            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val values = (resultJson \ "collection").as[JsArray].value

            values.size ==== 2

            (values.head \ "followers_count").as[Long] ==== 100
            (values.head \ "followings_count").as[Long] ==== 200

            (values.last \ "followers_count").as[Long] ==== 300
            (values.last \ "followings_count").as[Long] ==== 400
          }

          "with objects containing a user" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = objectsWithUsers

            override def userUrns = Seq(user2, user3)

            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val values = resultJson.as[JsArray].value

            values.size ==== 2

            (values.head \ "user" \ "followers_count").as[Long] ==== 100
            (values.head \ "user" \ "followings_count").as[Long] ==== 200

            (values.last \ "user" \ "followers_count").as[Long] ==== 300
            (values.last \ "user" \ "followings_count").as[Long] ==== 400
          }
        }

        "with like counts from mothership" >> {
          "with single object" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = user

            override def userUrns = Seq(user1)

            (resultJson \ "public_favorites_count").as[Long] ==== 123
          }

          "with users in the top level" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = users

            override def userUrns = Seq(user2, user3)

            val values = resultJson.as[JsArray].value

            values.size ==== 2

            (values.head \ "public_favorites_count").as[Long] ==== 234
            (values.last \ "public_favorites_count").as[Long] ==== 654
          }

          "with users in a collection" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = usersInCollection

            override def userUrns = Seq(user2, user3)

            val values = (resultJson \ "collection").as[JsArray].value

            values.size ==== 2

            (values.head \ "public_favorites_count").as[Long] ==== 987
            (values.last \ "public_favorites_count").as[Long] ==== 123
          }

          "with objects containing a user" in new LoadUserLikeCountsFromLieblingIsFalse {
            override def responseBodyFromPublicApi = objectsWithUsers

            override def userUrns = Seq(user2, user3)

            val values = resultJson.as[JsArray].value

            values.size ==== 2

            (values.head \ "user" \ "public_favorites_count").as[Long] ==== 456
            (values.last \ "user" \ "public_favorites_count").as[Long] ==== 765
          }
        }
      }
    }
  }
}
