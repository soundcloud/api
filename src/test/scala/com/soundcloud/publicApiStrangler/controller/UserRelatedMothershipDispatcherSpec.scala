package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.test.FakeUserAuthentication
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
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
    val repostsClientMock = mock[RepostsClient]
    val mothershipDispatcherMock = mock[DispatchToMothershipHandler]
    val request = new Request(mock[FinagleRequest])

    def loadUserLikeCountsFromLiebling: Boolean = false
    def loadUserRepostCountsFromReposts: Boolean = false

    val dispatcher = new UserRelatedMothershipDispatcher(
      userAuthenticationMock,
      mothershipDispatcherMock,
      followCountsClientMock,
      lieblingClientMock,
      () => Future.value(loadUserLikeCountsFromLiebling),
      repostsClientMock,
      () => Future.value(loadUserRepostCountsFromReposts)
    )

    val user1 = new Urn("soundcloud", "users", "183")
    val user2 = new Urn("soundcloud", "users", "1111")
    val user3 = new Urn("soundcloud", "users", "2222")

    def responseStatusFromMothership: Int = 200

    def responseBodyFromMothership: JsValue

    def responseBuilder = new ResponseBuilder()
      .status(responseStatusFromMothership)
      .body(responseBodyFromMothership.toString())

    def userUrns: Seq[Urn] = Seq.empty
    def followCountsSeq: Seq[FollowCounts] = Seq.empty
    def userTotalLikesList: List[UserTotalLikes] = List.empty

    def result = Await.result(dispatcher.dispatchToMothership(request)).build
    lazy val resultJson = Json.parse(result.getContentString())

    override def before: Any = {
      when(mothershipDispatcherMock.defaultHandling(any[HandlerRequest])).thenReturn(Future.value(responseBuilder.build))
      when(followCountsClientMock.counts(session, userUrns)).thenReturn(Future.value(followCountsSeq))
      when(lieblingClientMock.userTotalLikeCount(session, userUrns)).thenReturn(Future.value(userTotalLikesList))
    }

    override def after: Any = {
    }
  }

  "#dispatchToMothership" >> {
    "with a non-JSON response from mothership, it returns the same response body" in new Context {
      override def responseBodyFromMothership = JsNull
      override def responseBuilder = new ResponseBuilder()
        .status(responseStatusFromMothership)
        .body("Not a JSON response")

      Await.result(dispatcher.dispatchToMothership(request)).build.getContentString() ==== "Not a JSON response"
    }
    "with a non-OK status code from mothership, it returns the same status code" in new Context {
      override def responseStatusFromMothership = 500
      override def responseBodyFromMothership = user

      result.getStatusCode() ==== 500
    }
    "with an OK status code from mothership" >> {
      "it returns an object that matches the one from Mothership" in new Context {
        override def responseBodyFromMothership = user
        override def userUrns = Seq(user1)
        override def followCountsSeq = Seq(FollowCounts(user1, 100, 101)) // same as Mothership, so enrich is a noop

        resultJson ==== responseBodyFromMothership
      }

      "follow count enrichment" >> {
        "when follows does not return counts" >> {
          "defaults to zero for a single object" in new Context {
            override def responseBodyFromMothership = user
            override def userUrns = Seq(user1)

            (resultJson \ "followers_count").as[Long] ==== 0
            (resultJson \ "followings_count").as[Long] ==== 0
          }
          "defaults to zero for users in the top level" in new Context {
            override def responseBodyFromMothership = users
            override def userUrns = Seq(user2, user3)

            val collection = resultJson.as[JsArray].value
            (collection.head \ "followers_count").as[Long] ==== 0
            (collection.head \ "followings_count").as[Long] ==== 0
            (collection.last \ "followers_count").as[Long] ==== 0
            (collection.last \ "followings_count").as[Long] ==== 0
          }
          "defaults to zero for users in a collection" in new Context {
            override def responseBodyFromMothership = usersInCollection
            override def userUrns = Seq(user2, user3)

            val collection = (resultJson \ "collection").as[JsArray].value

            collection.size ==== 2
            (collection.head \ "followers_count").as[Long] ==== 0
            (collection.head \ "followings_count").as[Long] ==== 0
            (collection.last \ "followers_count").as[Long] ==== 0
            (collection.last \ "followings_count").as[Long] ==== 0
          }
          "defaults to zero for objects containing a user" in new Context {
            override def responseBodyFromMothership = objectsWithUsers
            override def userUrns = Seq(user2, user3)

            val collection = resultJson.as[JsArray].value

            collection.size ==== 2
            (collection.head \ "user" \ "followers_count").as[Long] ==== 0
            (collection.head \ "user" \ "followings_count").as[Long] ==== 0
            (collection.last \ "user" \ "followers_count").as[Long] ==== 0
            (collection.last \ "user" \ "followings_count").as[Long] ==== 0
          }
        }
        "when follows returns counts" >> {
          "is done for a single object" in new Context {
            override def responseBodyFromMothership = user
            override def userUrns = Seq(user1)
            override def followCountsSeq = Seq(FollowCounts(user1, 100, 200))

            (resultJson \ "followers_count").as[Long] ==== 100
            (resultJson \ "followings_count").as[Long] ==== 200
          }
          "is done for users in the top level" in new Context {
            override def responseBodyFromMothership = users
            override def userUrns = Seq(user2, user3)
            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val collection = resultJson.as[JsArray].value

            collection.size ==== 2
            (collection.head \ "followers_count").as[Long] ==== 100
            (collection.head \ "followings_count").as[Long] ==== 200
            (collection.last \ "followers_count").as[Long] ==== 300
            (collection.last \ "followings_count").as[Long] ==== 400
          }
          "is done for users in a collection" in new Context {
            override def responseBodyFromMothership = usersInCollection
            override def userUrns = Seq(user2, user3)
            override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

            val values = (resultJson \ "collection").as[JsArray].value

            values.size ==== 2
            (values.head \ "followers_count").as[Long] ==== 100
            (values.head \ "followings_count").as[Long] ==== 200
            (values.last \ "followers_count").as[Long] ==== 300
            (values.last \ "followings_count").as[Long] ==== 400
          }
          "is done for objects containing a user" in new Context {
            override def responseBodyFromMothership = objectsWithUsers
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
      }

      "like count enrichment" >> {
        "with rollout inactive" >> {
          "is not done for a single object" in new Context {
            override def responseBodyFromMothership = user
            override def userUrns = Seq(user1)

            (resultJson \ "public_favorites_count").as[Long] ==== 123
          }
          "is not done for users in the top level" in new Context {
            override def responseBodyFromMothership = users
            override def userUrns = Seq(user2, user3)

            val values = resultJson.as[JsArray].value

            values.size ==== 2
            (values.head \ "public_favorites_count").as[Long] ==== 234
            (values.last \ "public_favorites_count").as[Long] ==== 654
          }
          "is not done for users in a collection" in new Context {
            override def responseBodyFromMothership = usersInCollection
            override def userUrns = Seq(user2, user3)

            val values = (resultJson \ "collection").as[JsArray].value

            values.size ==== 2
            (values.head \ "public_favorites_count").as[Long] ==== 987
            (values.last \ "public_favorites_count").as[Long] ==== 123
          }
          "is not done for objects containing a user" in new Context {
            override def responseBodyFromMothership = objectsWithUsers
            override def userUrns = Seq(user2, user3)

            val values = resultJson.as[JsArray].value

            values.size ==== 2
            (values.head \ "user" \ "public_favorites_count").as[Long] ==== 456
            (values.last \ "user" \ "public_favorites_count").as[Long] ==== 765
          }
        }
        "with rollout active" >> {
          trait EnrichLikeCounts extends Context {
            override def loadUserLikeCountsFromLiebling = true
          }

          "and liebling not returning counts" >> {
            "defaults to zero for a single object" in new EnrichLikeCounts {
              override def responseBodyFromMothership = user
              override def userUrns = Seq(user1)

              (resultJson \ "public_favorites_count").as[Long] ==== 0
            }
            "defaults to zero for users in the top level" in new EnrichLikeCounts {
              override def responseBodyFromMothership = users
              override def userUrns = Seq(user2, user3)

              val values = resultJson.as[JsArray].value

              values.size ==== 2
              (values.head \ "public_favorites_count").as[Long] ==== 0
              (values.last \ "public_favorites_count").as[Long] ==== 0
            }
            "defaults to zero for users in a collection" in new EnrichLikeCounts {
              override def responseBodyFromMothership = usersInCollection
              override def userUrns = Seq(user2, user3)

              val values = (resultJson \ "collection").as[JsArray].value

              values.size ==== 2
              (values.head \ "public_favorites_count").as[Long] ==== 0
              (values.last \ "public_favorites_count").as[Long] ==== 0
            }
            "defaults to zero for objects containing a user" in new EnrichLikeCounts {
              override def responseBodyFromMothership = objectsWithUsers
              override def userUrns = Seq(user2, user3)

              val values = resultJson.as[JsArray].value

              values.size ==== 2
              (values.head \ "user" \ "public_favorites_count").as[Long] ==== 0
              (values.last \ "user" \ "public_favorites_count").as[Long] ==== 0
            }
          }
          "and liebling returning counts" >> {
            "is done for a single object" in new EnrichLikeCounts {
              override def responseBodyFromMothership = user
              override def userUrns = Seq(user1)
              override def userTotalLikesList = List(UserTotalLikes(user1, 100, 200))

              (resultJson \ "public_favorites_count").as[Long] ==== 300
            }
            "is done for users in the top level" in new EnrichLikeCounts {
              override def responseBodyFromMothership = users
              override def userUrns = Seq(user2, user3)
              override def userTotalLikesList = List(UserTotalLikes(user2, 100, 200), UserTotalLikes(user3, 300, 400))

              val values = resultJson.as[JsArray].value

              values.size ==== 2
              (values.head \ "public_favorites_count").as[Long] ==== 300
              (values.last \ "public_favorites_count").as[Long] ==== 700
            }
            "is done for users in a collection" in new EnrichLikeCounts {
              override def responseBodyFromMothership = usersInCollection
              override def userUrns = Seq(user2, user3)
              override def userTotalLikesList = List(UserTotalLikes(user2, 100, 0), UserTotalLikes(user3, 300, 1))

              val values = (resultJson \ "collection").as[JsArray].value

              values.size ==== 2
              (values.head \ "public_favorites_count").as[Long] ==== 100
              (values.last \ "public_favorites_count").as[Long] ==== 301
            }
            "is done for objects containing a user" in new EnrichLikeCounts {
              override def responseBodyFromMothership = objectsWithUsers
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
  }
}
