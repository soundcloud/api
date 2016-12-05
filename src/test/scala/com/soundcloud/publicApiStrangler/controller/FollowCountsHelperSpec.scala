package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.test.FakeUserAuthentication
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, LikesCount}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.finagle.http.{Request => FinagleRequest}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsArray, JsNull, JsValue, Json}

class FollowCountsHelperSpec  extends UnitSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val session = mock[UserSession]
    val userAuthenticationMock = new FakeUserAuthentication(session)
    val followCountsClientMock = mock[FollowCountsClient]
    val lieblingClientMock = mock[LieblingClient]
    val mothershipDispatcherMock = mock[DispatchToMothershipHandler]
    val request = new Request(mock[FinagleRequest])

    val helper = new CountsHelper {
      override def userAuthentication = userAuthenticationMock
      override def followCountsClient = followCountsClientMock
      override def mothershipDispatcher = mothershipDispatcherMock
      override def lieblingClient = lieblingClientMock
    }

    val user1 = new Urn("soundcloud", "users", "183")
    val user2 = new Urn("soundcloud", "users", "1111")
    val user3 = new Urn("soundcloud", "users", "2222")

    def responseStatus: Int = 200

    def responseBody: JsValue

    def responseBuilder = new ResponseBuilder()
      .status(responseStatus)
      .body(responseBody.toString())

    def userUrns: Seq[Urn] = Seq.empty
    def followCountsSeq: Seq[FollowCounts] = Seq.empty
    def likeCountsList: List[LikesCount] = List.empty

    def responseContent = Json.parse(Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString())

    override def before = {
      when(mothershipDispatcherMock.defaultHandling(any[HandlerRequest])) thenReturn Future.value(responseBuilder.build)
      when(followCountsClientMock.counts(session, userUrns)) thenReturn Future.value(followCountsSeq)
      when(lieblingClientMock.likeCounts(session, userUrns)) thenReturn Future.value(likeCountsList)
    }
  }

  "follow counts" >> {
    "with a non-JSON response" in new Context {
      override def responseBody = JsNull

      override def responseBuilder = new ResponseBuilder()
        .status(responseStatus)
        .body("No a JSON response")

      Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== "No a JSON response"
    }

    "with a non-OK status code" in new Context {
      override def responseStatus = 500

      override def responseBody = user

      responseContent ==== responseBody
    }

    "with an OK status code" >> {
      "with no follow counts from client" >> {
        "with single object" in new Context {
          override def responseBody = user

          override def userUrns = Seq(user1)
          responseContent ==== responseBody
        }

        "with users in the top level" in new Context {
          override def responseBody = users

          override def userUrns = Seq(user2, user3)

          responseContent ==== responseBody
        }

        "with users in a collection" in new Context {
          override def responseBody = usersInCollection

          override def userUrns = Seq(user2, user3)

          responseContent ==== responseBody
        }

        "with objects containing a user" in new Context {
          override def responseBody = objectsWithUsers

          override def userUrns = Seq(user2, user3)

          responseContent ==== responseBody
        }
      }

      "with no like counts from client" >> {
        "with single object" in new Context {
          override def responseBody = user

          override def userUrns = Seq(user1)
          responseContent ==== responseBody
        }

        "with users in the top level" in new Context {
          override def responseBody = users

          override def userUrns = Seq(user2, user3)

          responseContent ==== responseBody
        }

        "with users in a collection" in new Context {
          override def responseBody = usersInCollection

          override def userUrns = Seq(user2, user3)

          responseContent ==== responseBody
        }

        "with objects containing a user" in new Context {
          override def responseBody = objectsWithUsers

          override def userUrns = Seq(user2, user3)

          responseContent ==== responseBody
        }
      }

      "with follow counts from client" >> {
        "with single object" in new Context {
          override def responseBody = user

          override def userUrns = Seq(user1)

          override def followCountsSeq = Seq(FollowCounts(user1, 100, 200))

          val result = responseContent
          (result \ "followers_count").as[Long] ==== 100
          (result \ "followings_count").as[Long] ==== 200
        }

        "with users in the top level" in new Context {
          override def responseBody = users

          override def userUrns = Seq(user2, user3)

          override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

          val result = responseContent
          val values = result.as[JsArray].value

          values.size ==== 2

          (values.head \ "followers_count").as[Long] ==== 100
          (values.head \ "followings_count").as[Long] ==== 200

          (values.last \ "followers_count").as[Long] ==== 300
          (values.last \ "followings_count").as[Long] ==== 400
        }

        "with users in a collection" in new Context {
          override def responseBody = usersInCollection

          override def userUrns = Seq(user2, user3)

          override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

          val result = responseContent
          val values = (result \ "collection").as[JsArray].value

          values.size ==== 2

          (values.head \ "followers_count").as[Long] ==== 100
          (values.head \ "followings_count").as[Long] ==== 200

          (values.last \ "followers_count").as[Long] ==== 300
          (values.last \ "followings_count").as[Long] ==== 400
        }

        "with objects containing a user" in new Context {
          override def responseBody = objectsWithUsers

          override def userUrns = Seq(user2, user3)

          override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

          val result = responseContent
          val values = result.as[JsArray].value

          values.size ==== 2

          (values.head \ "user" \ "followers_count").as[Long] ==== 100
          (values.head \ "user" \ "followings_count").as[Long] ==== 200

          (values.last \ "user" \ "followers_count").as[Long] ==== 300
          (values.last \ "user" \ "followings_count").as[Long] ==== 400
        }
      }

      "with like counts from client" >> {
        "with single object" in new Context {
          override def responseBody = user

          override def userUrns = Seq(user1)

          override def likeCountsList = List(LikesCount(user1, 100))

          (responseContent \ "public_favorites_count").as[Long] ==== 100
        }

        "with users in the top level" in new Context {
          override def responseBody = users

          override def userUrns = Seq(user2, user3)

          override def likeCountsList = List(LikesCount(user2, 100), LikesCount(user3, 300))

          val result = responseContent
          val values = result.as[JsArray].value

          values.size ==== 2

          (values.head \ "public_favorites_count").as[Long] ==== 100
          (values.last \ "public_favorites_count").as[Long] ==== 300
        }

        "with users in a collection" in new Context {
          override def responseBody = usersInCollection

          override def userUrns = Seq(user2, user3)

          override def likeCountsList = List(LikesCount(user2, 100), LikesCount(user3, 300))

          val result = Json.parse(Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString())
          val values = (result \ "collection").as[JsArray].value

          values.size ==== 2

          (values.head \ "public_favorites_count").as[Long] ==== 100
          (values.last \ "public_favorites_count").as[Long] ==== 300
        }

        "with objects containing a user" in new Context {
          override def responseBody = objectsWithUsers

          override def userUrns = Seq(user2, user3)

          override def likeCountsList = List(LikesCount(user2, 100), LikesCount(user3, 300))

          val result = Json.parse(Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString())
          val values = result.as[JsArray].value

          values.size ==== 2

          (values.head \ "user" \ "public_favorites_count").as[Long] ==== 100
          (values.last \ "user" \ "public_favorites_count").as[Long] ==== 300
        }
      }
    }
  }
}
